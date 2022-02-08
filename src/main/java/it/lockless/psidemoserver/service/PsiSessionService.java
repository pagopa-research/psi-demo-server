package it.lockless.psidemoserver.service;

import it.lockless.psidemoserver.config.PsiKey;
import it.lockless.psidemoserver.config.StoredAlgorithmKey;
import it.lockless.psidemoserver.entity.PsiSession;
import it.lockless.psidemoserver.entity.SerializedBloomFilter;
import it.lockless.psidemoserver.entity.enumeration.Algorithm;
import it.lockless.psidemoserver.mapper.AlgorithmMapper;
import it.lockless.psidemoserver.model.BloomFilterDTO;
import it.lockless.psidemoserver.model.PsiAlgorithmParameterDTO;
import it.lockless.psidemoserver.model.PsiClientSessionDTO;
import it.lockless.psidemoserver.repository.PsiSessionRepository;
import it.lockless.psidemoserver.util.GlobalVariable;
import it.lockless.psidemoserver.util.exception.CustomRuntimeException;
import it.lockless.psidemoserver.util.exception.KeyNotAvailableException;
import it.lockless.psidemoserver.util.exception.SessionExpiredException;
import it.lockless.psidemoserver.util.exception.SessionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import psi.cache.PsiCacheProvider;
import psi.exception.UnsupportedKeySizeException;
import psi.model.PsiAlgorithm;
import psi.model.PsiAlgorithmParameter;
import psi.model.PsiClientSession;
import psi.model.PsiRuntimeConfiguration;
import psi.server.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class PsiSessionService {

    private static final Logger log = LoggerFactory.getLogger(PsiSessionService.class);

    @Value("${session.expiration.minutes}")
    private int minutesBeforeSessionExpiration;

    @Value("${bloomfilter.enabled}")
    private boolean bloomFilterEnabled;

    private final PsiSessionRepository psiSessionRepository;

    private final StoredAlgorithmKey storedAlgorithmKey;

    private PsiCacheProvider psiCacheProvider;

    private final BloomFilterService bloomFilterService;

    @Autowired(required = false)
    private void setPsiCacheProvider(PsiCacheProvider psiCacheProvider){
        this.psiCacheProvider = psiCacheProvider;
    }

    public PsiSessionService(PsiSessionRepository psiSessionRepository, StoredAlgorithmKey storedAlgorithmKey, BloomFilterService bloomFilterService) {
        this.psiSessionRepository = psiSessionRepository;
        this.storedAlgorithmKey = storedAlgorithmKey;
        this.bloomFilterService = bloomFilterService;
    }

    private Instant getExpirationTime(){
        log.trace("Calling getExpirationTime");
        return Instant.now().plus(minutesBeforeSessionExpiration, ChronoUnit.MINUTES);
    }

    // Build ServerKeyDescription
    private PsiServerKeyDescription buildPsiServerKeyDescription(PsiAlgorithm psiAlgorithm, PsiKey psiKey){
        log.debug("Calling buildPsiServerKeyDescription with psiAlgorithm = {}, psiKey = {}", psiAlgorithm, psiKey);
        if (psiAlgorithm.equals(PsiAlgorithm.BS))
            return PsiServerKeyDescriptionFactory.createBsServerKeyDescription(psiKey.getPrivateKey(), psiKey.getPublicKey(), psiKey.getModulus());
        else if (psiAlgorithm.equals(PsiAlgorithm.DH))
            return PsiServerKeyDescriptionFactory.createDhServerKeyDescription(psiKey.getPrivateKey(), psiKey.getModulus());
        else if (psiAlgorithm.equals(PsiAlgorithm.ECBS))
            return PsiServerKeyDescriptionFactory.createEcBsServerKeyDescription(psiKey.getPrivateKey(), psiKey.getPublicKey(), psiKey.getEcSpecName());
        else if (psiAlgorithm.equals(PsiAlgorithm.ECDH))
            return PsiServerKeyDescriptionFactory.createEcDhServerKeyDescription(psiKey.getPrivateKey(), psiKey.getEcSpecName());
        throw new CustomRuntimeException("The algorithm "+psiAlgorithm+" is not supported");
    }

    public PsiClientSessionDTO initSession(PsiAlgorithmParameterDTO psiAlgorithmParameterDTO) throws UnsupportedKeySizeException {
        log.info("Calling initSession with psiAlgorithmParameterDTO = {}", psiAlgorithmParameterDTO);

        PsiAlgorithmParameter psiAlgorithmParameter = psiAlgorithmParameterDTO.getContent();

        // Retrieve the key corresponding to the pair <algorithm, keySIze>
        Optional<PsiKey> psiKeyOptional = storedAlgorithmKey.findByAlgorithmAndKeySize(
                AlgorithmMapper.toEntity(psiAlgorithmParameter.getAlgorithm()),
                psiAlgorithmParameter.getKeySize());

        if(!psiKeyOptional.isPresent())
            throw new UnsupportedKeySizeException(psiAlgorithmParameter.getAlgorithm(), psiAlgorithmParameter.getKeySize());

        // Build ServerKeyDescription
        PsiServerKeyDescription psiServerKeyDescription = buildPsiServerKeyDescription(psiAlgorithmParameter.getAlgorithm(), psiKeyOptional.get());

        // Init a new server session
        PsiServerSession psiServerSession = PsiServerFactory.initSession(psiAlgorithmParameter, psiServerKeyDescription, psiCacheProvider);

        // Storing the session into the DB
        PsiSession psiSession = new PsiSession();
        psiSession.setCacheEnabled(psiCacheProvider != null);
        psiSession.setKeySize(psiAlgorithmParameter.getKeySize());
        psiSession.setAlgorithm(AlgorithmMapper.toEntity(psiAlgorithmParameter.getAlgorithm()));
        psiSession.setKeyId(psiKeyOptional.get().getKeyId());
        psiSession.setExpiration(getExpirationTime());
        psiSession.setSessionId((long)(Math.random() * (Long.MAX_VALUE)));
        psiSessionRepository.save(psiSession);

        // Building the response containing the PsiClientSession used by the client to initialize its session
        PsiClientSessionDTO psiClientSessionDTO = new PsiClientSessionDTO();
        psiClientSessionDTO.setExpiration(psiSession.getExpiration());
        psiClientSessionDTO.setSessionId(psiSession.getSessionId());
        psiClientSessionDTO.setPsiClientSession(PsiClientSession.getFromServerSession(psiServerSession));

        // If bloom filter is enabled, get the last generated bloom filter (if available) and set it
        if(bloomFilterEnabled){
            Optional<SerializedBloomFilter> serializedBloomFilterOptional = bloomFilterService.getLastSerializedBloomFilter();
            serializedBloomFilterOptional.ifPresent(serializedBloomFilter -> psiClientSessionDTO.setBloomFilterDTO(new BloomFilterDTO(serializedBloomFilter)));
        }

        return psiClientSessionDTO;
    }

    PsiServer loadPsiServerBySessionId(long sessionId) throws SessionNotFoundException, SessionExpiredException {
        log.debug("Calling loadPsiServerBySessionId with sessionId = {}", sessionId);
        PsiServer psiServer = PsiServerFactory.loadSession(getPsiServerSession(sessionId), psiCacheProvider);
        psiServer.setConfiguration(new PsiRuntimeConfiguration(GlobalVariable.DEFAULT_THREADS));
        return psiServer;
    }

    private PsiServerSession buildPsiServerSession(Algorithm algorithm, int keySize, PsiKey psiKey){
        log.trace("Calling buildPsiServerSession with algorithm = {}, keySize = {}", algorithm, keySize);
        // Build the ServerKeyDescription to be passed into the PsiServerSession
        PsiServerKeyDescription psiServerKeyDescription = buildPsiServerKeyDescription(AlgorithmMapper.toPsiAlgorithm(algorithm), psiKey);

        // Build the ServerSession used to load the PsiServer
        return new PsiServerSession(AlgorithmMapper.toPsiAlgorithm(algorithm), keySize, psiCacheProvider != null, psiServerKeyDescription);
    }

    private PsiServerSession getPsiServerSession(long sessionId) throws SessionNotFoundException, SessionExpiredException {
        log.trace("Calling getPsiServerSession with sessionId = {}", sessionId);

        // Retrieve the session information from the database.
        // These information will be used to build the PsiServerSession object required to create the PsiServe object
        PsiSession psiSession = psiSessionRepository.findBySessionId(sessionId)
                .orElseThrow(SessionNotFoundException::new);

        // Check if the session is expired
        if(psiSession.getExpiration().isBefore(Instant.now()))
            throw new SessionExpiredException();

        // Retrieve the key used by the current session
        PsiKey psiKey = storedAlgorithmKey.findByKeyId(psiSession.getKeyId())
                    .orElseThrow(KeyNotAvailableException::new);

        // Build the PsiServerSession object
        return buildPsiServerSession(psiSession.getAlgorithm(), psiSession.getKeySize(), psiKey);
    }

    // Used to check the status of the session
    public PsiClientSessionDTO getPsiClientSessionDTO(long sessionId) throws SessionNotFoundException {
        log.trace("Calling getPsiClientSessionDTO with sessionId = {}", sessionId);

        // Retrieve the session information from the database
        PsiSession psiSession = psiSessionRepository.findBySessionId(sessionId)
                .orElseThrow(SessionNotFoundException::new);

        // Retrieve the key used by the current session
        PsiKey psiKey =  storedAlgorithmKey.findByKeyId(psiSession.getKeyId())
                    .orElseThrow(KeyNotAvailableException::new);

        // Build the PsiServerSession object
        PsiServerSession psiServerSession =
                buildPsiServerSession(psiSession.getAlgorithm(), psiSession.getKeySize(), psiKey);

        PsiClientSessionDTO psiClientSessionDTO = new PsiClientSessionDTO();
        psiClientSessionDTO.setExpiration(psiSession.getExpiration());
        psiClientSessionDTO.setSessionId(psiSession.getId());
        psiClientSessionDTO.setPsiClientSession(PsiClientSession.getFromServerSession(psiServerSession));

        // If bloom filter is enabled, get the last generated bloom filter (if available) and set it
        if(bloomFilterEnabled){
            Optional<SerializedBloomFilter> serializedBloomFilterOptional = bloomFilterService.getLastSerializedBloomFilter();
            serializedBloomFilterOptional.ifPresent(serializedBloomFilter -> psiClientSessionDTO.setBloomFilterDTO(new BloomFilterDTO(serializedBloomFilter)));
        }
        return psiClientSessionDTO;
    }

}
