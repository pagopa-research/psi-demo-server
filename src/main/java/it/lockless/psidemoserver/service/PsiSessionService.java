package it.lockless.psidemoserver.service;

import it.lockless.psidemoserver.config.StoredAlgorithmKey;
import it.lockless.psidemoserver.entity.PsiKey;
import it.lockless.psidemoserver.entity.PsiSession;
import it.lockless.psidemoserver.entity.enumeration.Algorithm;
import it.lockless.psidemoserver.mapper.AlgorithmMapper;
import it.lockless.psidemoserver.model.PsiSessionWrapperDTO;
import it.lockless.psidemoserver.repository.PsiSessionRepository;
import it.lockless.psidemoserver.service.cache.PsiCacheProviderImplementation;
import it.lockless.psidemoserver.util.exception.KeyNotAvailableException;
import it.lockless.psidemoserver.util.exception.SessionExpiredException;
import it.lockless.psidemoserver.util.exception.SessionNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import psi.dto.PsiAlgorithmParameterDTO;
import psi.mapper.SessionDTOMapper;
import psi.server.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class PsiSessionService {

    @Value("${session.expiration.minutes}")
    private int minutesBeforeSessionExpiration;

    private final PsiSessionRepository psiSessionRepository;

    private final StoredAlgorithmKey storedAlgorithmKey;

    private final PsiCacheProviderImplementation psiCacheProviderImplementation;

    public PsiSessionService(PsiSessionRepository psiSessionRepository, StoredAlgorithmKey storedAlgorithmKey, PsiCacheProviderImplementation psiCacheProviderImplementation) {
        this.psiSessionRepository = psiSessionRepository;
        this.storedAlgorithmKey = storedAlgorithmKey;
        this.psiCacheProviderImplementation = psiCacheProviderImplementation;
    }

    private Instant getExpirationTime(){
        return Instant.now().plus(minutesBeforeSessionExpiration, ChronoUnit.MINUTES);
    }

    public PsiSessionWrapperDTO initSession(PsiAlgorithmParameterDTO sessionParameterDTO) {
        // fulfill PsiAlgorithmParameterDTO
        PsiAlgorithmParameterDTO psiAlgorithmParameterDTO = new PsiAlgorithmParameterDTO();
        psiAlgorithmParameterDTO.setAlgorithm(sessionParameterDTO.getAlgorithm());
        psiAlgorithmParameterDTO.setKeySize(sessionParameterDTO.getKeySize());

        // Retrieve the key corresponding to the pair <algorithm, keySIze>
        PsiKey psiKey = storedAlgorithmKey.findByAlgorithmAndKeySize(
                AlgorithmMapper.toEntity(sessionParameterDTO.getAlgorithm()),
                sessionParameterDTO.getKeySize())
                .orElseThrow(KeyNotAvailableException::new);

        // Build ServerKeyDescription
        PsiServerKeyDescription psiServerKeyDescription =
                PsiServerKeyDescriptionFactory.createBsServerKeyDescription(psiKey.getPrivateKey(), psiKey.getPublicKey(), psiKey.getModulus(), psiKey.getKeyId());

        // Init a new server session
        PsiServerSession psiServerSession = PsiServerFactory.initSession(psiAlgorithmParameterDTO, psiServerKeyDescription, psiCacheProviderImplementation);

        // Storing the session into the DB
        PsiSession psiSession = new PsiSession();
        psiSession.setCacheEnabled(true);
        psiSession.setKeySize(sessionParameterDTO.getKeySize());
        psiSession.setAlgorithm(AlgorithmMapper.toEntity(sessionParameterDTO.getAlgorithm()));
        psiSession.setKeyId(psiKey.getKeyId());
        psiSession.setExpiration(getExpirationTime());
        psiSessionRepository.save(psiSession);

        PsiSessionWrapperDTO psiSessionWrapperDTO = new PsiSessionWrapperDTO();
        psiSessionWrapperDTO.setExpiration(psiSession.getExpiration());
        psiSessionWrapperDTO.setSessionId(psiSession.getId());
        psiSessionWrapperDTO.setPsiSessionDTO(
                SessionDTOMapper.getSessionDtoFromServerSession(psiServerSession));

        return psiSessionWrapperDTO;
    }

    PsiServer loadPsiServerBySessionId(long sessionId) throws SessionNotFoundException, SessionExpiredException {
        PsiServerSession psiServerSession = getPsiServerSession(sessionId);
        return PsiServerFactory.loadSession(psiServerSession, psiCacheProviderImplementation);
    }

    //TODO: spostare questo metodo nella libreria? (magari già c'è) Far diventare gli algoritmi un enum?
    private PsiServerSession buildPsiServerSession(Algorithm algorithm, int keySize, String modulus, String privateKey, String publicKey, long keyId){
        // Fulfill PsiAlgorithmParameterDTO
        PsiAlgorithmParameterDTO psiAlgorithmParameterDTO = new PsiAlgorithmParameterDTO();
        psiAlgorithmParameterDTO.setAlgorithm(AlgorithmMapper.toDTO(algorithm));
        psiAlgorithmParameterDTO.setKeySize(keySize);

        // Build ServerKeyDescription
        PsiServerKeyDescription psiServerKeyDescription =
                PsiServerKeyDescriptionFactory.createBsServerKeyDescription(privateKey, publicKey, modulus, keyId);

        // Init a new server session
        return PsiServerFactory.initSession(psiAlgorithmParameterDTO, psiServerKeyDescription, psiCacheProviderImplementation);
    }

    private PsiServerSession getPsiServerSession(long sessionId) throws SessionNotFoundException, SessionExpiredException {
        PsiSession psiSession = psiSessionRepository.findById(sessionId)
                .orElseThrow(SessionNotFoundException::new);
        if(psiSession.getExpiration().isBefore(Instant.now()))
            throw new SessionExpiredException();
        PsiKey psiKey = storedAlgorithmKey.findByKeyId(psiSession.getKeyId())
                    .orElseThrow(KeyNotAvailableException::new);

        return buildPsiServerSession(psiSession.getAlgorithm(), psiSession.getKeySize(), psiKey.getModulus(), psiKey.getPrivateKey(), psiKey.getPublicKey(), psiKey.getKeyId());
    }

    public PsiSessionWrapperDTO getPsiSessionWrapperDTO(long sessionId) throws SessionNotFoundException {
        PsiSession psiSession = psiSessionRepository.findById(sessionId)
                .orElseThrow(SessionNotFoundException::new);
        PsiKey psiKey =  storedAlgorithmKey.findByKeyId(psiSession.getKeyId())
                    .orElseThrow(KeyNotAvailableException::new);

        PsiServerSession psiServerSession =
                buildPsiServerSession(psiSession.getAlgorithm(), psiSession.getKeySize(), psiKey.getModulus(), psiKey.getPrivateKey(), psiKey.getPublicKey(), psiKey.getKeyId());

        PsiSessionWrapperDTO psiSessionWrapperDTO = new PsiSessionWrapperDTO();
        psiSessionWrapperDTO.setExpiration(psiSession.getExpiration());
        psiSessionWrapperDTO.setSessionId(psiSession.getId());
        psiSessionWrapperDTO.setPsiSessionDTO(
                SessionDTOMapper.getSessionDtoFromServerSession(psiServerSession));

        return psiSessionWrapperDTO;
    }

}
