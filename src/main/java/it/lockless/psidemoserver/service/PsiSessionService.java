package it.lockless.psidemoserver.service;

import it.lockless.psidemoserver.config.PsiKey;
import it.lockless.psidemoserver.entity.PsiSession;
import it.lockless.psidemoserver.entity.SerializedBloomFilter;
import it.lockless.psidemoserver.mapper.AlgorithmMapper;
import it.lockless.psidemoserver.model.BloomFilterDTO;
import it.lockless.psidemoserver.model.PsiAlgorithmParameterDTO;
import it.lockless.psidemoserver.model.PsiClientSessionDTO;
import it.lockless.psidemoserver.repository.PsiSessionRepository;
import it.lockless.psidemoserver.util.exception.AlgorithmInvalidKeyException;
import it.lockless.psidemoserver.util.exception.SessionExpiredException;
import it.lockless.psidemoserver.util.exception.SessionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import psi.PsiServerFactory;
import psi.PsiServerKeyDescription;
import psi.PsiServerSession;
import psi.cache.PsiCacheProvider;
import psi.exception.UnsupportedKeySizeException;
import psi.model.PsiAlgorithmParameter;
import psi.model.PsiClientSession;
import psi.model.PsiRuntimeConfiguration;
import psi.server.PsiServer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * It offers functionalities used to manage the sessions (creation and retrieving).
 */

@Service
public class PsiSessionService {

    private static final Logger log = LoggerFactory.getLogger(PsiSessionService.class);

    @Value("${session.expiration.minutes}")
    private int minutesBeforeSessionExpiration;

    @Value("${bloomfilter.enabled}")
    private boolean bloomFilterEnabled;

    @Value("${threads: 8}")
    private int numThreads;

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

    /**
     * Computes the expiration time starting from the current time.
     * @return the Instant containing the expiration time
     */
    private Instant getExpirationTime(){
        log.trace("Calling getExpirationTime");
        return Instant.now().plus(minutesBeforeSessionExpiration, ChronoUnit.MINUTES);
    }

    /**
     * Initializes and stores a new session based on the input algorithm parameters
     * and build the PsiClientSessionDTO to be returned to the client.
     * @param psiAlgorithmParameterDTO the psiAlgorithmParameter selected by the client
     * @return a PsiClientSessionDTO containing the information required to setup the client side session
     * @throws AlgorithmInvalidKeyException if the selected key size is not supported for the selected algorithm
     */
    public PsiClientSessionDTO initSession(PsiAlgorithmParameterDTO psiAlgorithmParameterDTO) throws AlgorithmInvalidKeyException {
        log.info("Calling initSession with psiAlgorithmParameterDTO = {}", psiAlgorithmParameterDTO);

        PsiAlgorithmParameter psiAlgorithmParameter = psiAlgorithmParameterDTO.getContent();

        // Retrieves the key corresponding to the pair <algorithm, keySIze>
        Optional<PsiKey> psiKeyOptional = psiKeyService.findByPsiAlgorithmParameter(psiAlgorithmParameter);

        PsiServerSession psiServerSession;
        Long psiKeyId = psiKeyOptional.map(PsiKey::getKeyId).orElse(null);
        // If a key is available it is used in the new server session
        try {
            if(psiKeyOptional.isPresent()){
                // Builds ServerKeyDescription with the stored key
                PsiServerKeyDescription psiServerKeyDescription = psiKeyService.buildPsiServerKeyDescription(psiKeyOptional.get());
                psiServerSession = PsiServerFactory.initSession(psiAlgorithmParameter, psiServerKeyDescription, psiCacheProvider);

            } else {
                psiServerSession = PsiServerFactory.initSession(psiAlgorithmParameter, psiCacheProvider);
                psiKeyId = psiKeyService.storePsiServerKeyDescription(psiAlgorithmParameter, psiServerSession.getPsiServerKeyDescription());
            }
        } catch (UnsupportedKeySizeException e) {
            throw new AlgorithmInvalidKeyException("The keySize " + psiAlgorithmParameter.getKeySize() + " is not supported for the algorithm " + psiAlgorithmParameter.getAlgorithm());
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

        // If bloom filter is enabled, gets the last generated bloom filter (if available) and sets it
        if(bloomFilterEnabled){
            Optional<SerializedBloomFilter> serializedBloomFilterOptional = bloomFilterService.getLastSerializedBloomFilter();
            serializedBloomFilterOptional.ifPresent(serializedBloomFilter -> psiClientSessionDTO.setBloomFilterDTO(new BloomFilterDTO(serializedBloomFilter)));
        }

        return psiClientSessionDTO;
    }

    /**
     * Builds a PsiServer instance based on the input sessionId.
     * The information about the session are retrieved from the database.
     * @param sessionId the id identifying the session associated to the client
     * @return a PsiServer initialized with the requested session
     * @throws SessionNotFoundException if the sessionId does not correspond to any session in the database
     * @throws SessionExpiredException  if the session is expired
     */
    PsiServer loadPsiServerBySessionId(long sessionId) throws SessionNotFoundException, SessionExpiredException {
        log.debug("Calling loadPsiServerBySessionId with sessionId = {}", sessionId);

        // Retrieves the session information from the database.
        // These information will be used to build the PsiServerSession object required to create the PsiServe object
        PsiSession psiSession = psiSessionRepository.findBySessionId(sessionId)
                .orElseThrow(SessionNotFoundException::new);

        // Checks if the session is expired
        if(psiSession.getExpiration().isBefore(Instant.now()))
            throw new SessionExpiredException();

        // Retrieves the key used by the current session
        PsiServerKeyDescription psiServerKeyDescription = psiKeyService.findAndBuildByKeyId(psiSession.getKeyId());

        // Build the ServerSession used to load the PsiServer
        PsiServerSession psiServerSession = new PsiServerSession(
                AlgorithmMapper.toPsiAlgorithm(psiSession.getAlgorithm()),
                psiSession.getKeySize(),
                psiSession.getCacheEnabled(),
                psiServerKeyDescription);

        // Initialize the PsiServer with the PsiServerSession
        PsiServer psiServer = PsiServerFactory.loadSession(psiServerSession, psiCacheProvider);
        psiServer.setConfiguration(new PsiRuntimeConfiguration(numThreads));
        return psiServer;
    }

    /**
     * Retrieve the information about the sessionId and build the PsiClientSessionDTO.
     * The information about the session are retrieved from the database.
     * @param sessionId the id identifying the session associated to the client
     * @return a PsiServer initialized with the requested session
     * @throws SessionNotFoundException if the sessionId does not correspond to any session in the database
     */
    public PsiClientSessionDTO getPsiClientSessionDTO(long sessionId) throws SessionNotFoundException {
        log.trace("Calling getPsiClientSessionDTO with sessionId = {}", sessionId);

        // Retrieves the session information from the database
        PsiSession psiSession = psiSessionRepository.findBySessionId(sessionId)
                .orElseThrow(SessionNotFoundException::new);

        // Retrieves the key used by the current session
        PsiServerKeyDescription psiServerKeyDescription = psiKeyService.findAndBuildByKeyId(psiSession.getKeyId());

        // Builds the PsiServerSession object
        PsiServerSession psiServerSession =new PsiServerSession(
                AlgorithmMapper.toPsiAlgorithm(psiSession.getAlgorithm()),
                psiSession.getKeySize(),
                psiSession.getCacheEnabled(),
                psiServerKeyDescription);

        PsiClientSessionDTO psiClientSessionDTO = new PsiClientSessionDTO();
        psiClientSessionDTO.setExpiration(psiSession.getExpiration());
        psiClientSessionDTO.setSessionId(psiSession.getId());
        psiClientSessionDTO.setPsiClientSession(PsiClientSession.getFromServerSession(psiServerSession));

        // If bloom filter is enabled, gets the last generated bloom filter (if available) and set it
        if(bloomFilterEnabled){
            Optional<SerializedBloomFilter> serializedBloomFilterOptional = bloomFilterService.getLastSerializedBloomFilter();
            serializedBloomFilterOptional.ifPresent(serializedBloomFilter -> psiClientSessionDTO.setBloomFilterDTO(new BloomFilterDTO(serializedBloomFilter)));
        }
        return psiClientSessionDTO;
    }

}
