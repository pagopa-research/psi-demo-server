package it.lockless.psidemoserver.service;

import it.lockless.psidemoserver.config.PsiKey;
import it.lockless.psidemoserver.entity.PsiSession;
import it.lockless.psidemoserver.entity.SerializedBloomFilter;
import it.lockless.psidemoserver.mapper.AlgorithmMapper;
import it.lockless.psidemoserver.model.BloomFilterDTO;
import it.lockless.psidemoserver.model.PsiAlgorithmParameterDTO;
import it.lockless.psidemoserver.model.PsiClientSessionDTO;
import it.lockless.psidemoserver.repository.PsiSessionRepository;
import it.lockless.psidemoserver.util.GlobalVariable;
import it.lockless.psidemoserver.util.exception.SessionExpiredException;
import it.lockless.psidemoserver.util.exception.SessionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import psi.PsiServerFactory;
import psi.PsiServerSession;
import psi.cache.PsiCacheProvider;
import psi.exception.UnsupportedKeySizeException;
import psi.model.PsiAlgorithmParameter;
import psi.model.PsiClientSession;
import psi.model.PsiRuntimeConfiguration;
import psi.server.PsiServer;
import psi.server.PsiServerKeyDescription;

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

    private final PsiKeyService psiKeyService;

    private PsiCacheProvider psiCacheProvider;

    private final BloomFilterService bloomFilterService;

    @Autowired(required = false)
    private void setPsiCacheProvider(PsiCacheProvider psiCacheProvider){
        this.psiCacheProvider = psiCacheProvider;
    }

    public PsiSessionService(PsiSessionRepository psiSessionRepository, PsiKeyService psiKeyService, BloomFilterService bloomFilterService) {
        this.psiSessionRepository = psiSessionRepository;
        this.psiKeyService = psiKeyService;
        this.bloomFilterService = bloomFilterService;
    }

    // Compute the expiration time starting from the current time
    private Instant getExpirationTime(){
        log.trace("Calling getExpirationTime");
        return Instant.now().plus(minutesBeforeSessionExpiration, ChronoUnit.MINUTES);
    }

    // Initialize and store a new session based on the input algorithm parameters
    // and build the PsiClientSessionDTO to be returned to the client
    public PsiClientSessionDTO initSession(PsiAlgorithmParameterDTO psiAlgorithmParameterDTO) throws UnsupportedKeySizeException {
        log.info("Calling initSession with psiAlgorithmParameterDTO = {}", psiAlgorithmParameterDTO);

        PsiAlgorithmParameter psiAlgorithmParameter = psiAlgorithmParameterDTO.getContent();

        // Retrieve the key corresponding to the pair <algorithm, keySIze>
        Optional<PsiKey> psiKeyOptional = psiKeyService.findByPsiAlgorithmParameter(psiAlgorithmParameter);

        PsiServerSession psiServerSession;
        Long psiKeyId = psiKeyOptional.map(PsiKey::getKeyId).orElse(null);
        // If a key is available it is used in the new server session
        if(psiKeyOptional.isPresent()){
            // Build ServerKeyDescription with the stored key
            PsiServerKeyDescription psiServerKeyDescription = psiKeyService.buildPsiServerKeyDescription(psiKeyOptional.get());
            psiServerSession = PsiServerFactory.initSession(psiAlgorithmParameter, psiServerKeyDescription, psiCacheProvider);
        } else {
            psiServerSession = PsiServerFactory.initSession(psiAlgorithmParameter, psiCacheProvider);
            psiKeyId = psiKeyService.storePsiServerKeyDescription(psiAlgorithmParameter, psiServerSession.getPsiServerKeyDescription());
        }

        // Storing the session into the DB
        PsiSession psiSession = new PsiSession();
        psiSession.setCacheEnabled(psiCacheProvider != null);
        psiSession.setKeySize(psiAlgorithmParameter.getKeySize());
        psiSession.setAlgorithm(AlgorithmMapper.toEntity(psiAlgorithmParameter.getAlgorithm()));
        psiSession.setKeyId(psiKeyId);
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

    // Build a PsiServer based on the input sessionId
    PsiServer loadPsiServerBySessionId(long sessionId) throws SessionNotFoundException, SessionExpiredException {
        log.debug("Calling loadPsiServerBySessionId with sessionId = {}", sessionId);
        PsiServer psiServer = PsiServerFactory.loadSession(getPsiServerSession(sessionId), psiCacheProvider);
        psiServer.setConfiguration(new PsiRuntimeConfiguration(GlobalVariable.DEFAULT_THREADS));
        return psiServer;
    }

    // Retrieve the information about the sessionId and build the PsiServerSession
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
        PsiServerKeyDescription psiServerKeyDescription = psiKeyService.findAndBuildByKeyId(psiSession.getKeyId());

        // Build the ServerSession used to load the PsiServer
        return new PsiServerSession(
                AlgorithmMapper.toPsiAlgorithm(psiSession.getAlgorithm()),
                psiSession.getKeySize(),
                psiSession.getCacheEnabled(),
                psiServerKeyDescription);
    }

    // Retrieve the information about the sessionId and build the PsiClientSessionDTO
    public PsiClientSessionDTO getPsiClientSessionDTO(long sessionId) throws SessionNotFoundException {
        log.trace("Calling getPsiClientSessionDTO with sessionId = {}", sessionId);

        // Retrieve the session information from the database
        PsiSession psiSession = psiSessionRepository.findBySessionId(sessionId)
                .orElseThrow(SessionNotFoundException::new);

        // Retrieve the key used by the current session
        PsiServerKeyDescription psiServerKeyDescription = psiKeyService.findAndBuildByKeyId(psiSession.getKeyId());

        // Build the PsiServerSession object
        PsiServerSession psiServerSession =new PsiServerSession(
                AlgorithmMapper.toPsiAlgorithm(psiSession.getAlgorithm()),
                psiSession.getKeySize(),
                psiSession.getCacheEnabled(),
                psiServerKeyDescription);

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
