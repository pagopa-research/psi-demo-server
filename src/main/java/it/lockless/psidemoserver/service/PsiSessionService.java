package it.lockless.psidemoserver.service;

import it.lockless.psidemoserver.config.StoredAlgorithmKey;
import it.lockless.psidemoserver.entity.PsiKey;
import it.lockless.psidemoserver.entity.PsiSession;
import it.lockless.psidemoserver.entity.enumeration.Algorithm;
import it.lockless.psidemoserver.mapper.AlgorithmMapper;
import it.lockless.psidemoserver.model.PsiSessionWrapperDTO;
import it.lockless.psidemoserver.repository.PsiKeyRepository;
import it.lockless.psidemoserver.repository.PsiSessionRepository;
import it.lockless.psidemoserver.service.cache.PsiCacheProviderImplementation;
import it.lockless.psidemoserver.util.exception.AlgorithmNotSupportedException;
import it.lockless.psidemoserver.util.exception.KeyNotAvailableException;
import it.lockless.psidemoserver.util.exception.SessionExpiredException;
import it.lockless.psidemoserver.util.exception.SessionNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import psi.dto.PsiAlgorithmParameterDTO;
import psi.mapper.SessionDtoMapper;
import psi.server.PsiServer;
import psi.server.PsiServerFactory;
import psi.server.algorithm.bs.model.BsPsiServerKeyDescription;
import psi.server.algorithm.bs.model.BsPsiServerSession;
import psi.server.model.PsiServerKeyDescription;
import psi.server.model.PsiServerSession;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class PsiSessionService {

    @Value("${session.expiration.minutes}")
    private int minutesBeforeSessionExpiration;

    private final PsiSessionRepository psiSessionRepository;

    private final PsiKeyRepository psiKeyRepository;

    private final StoredAlgorithmKey storedAlgorithmKey;

    private final PsiCacheProviderImplementation psiCacheProviderImplementation;

    public PsiSessionService(PsiSessionRepository psiSessionRepository, PsiKeyRepository psiKeyRepository, StoredAlgorithmKey storedAlgorithmKey, PsiCacheProviderImplementation psiCacheProviderImplementation) {
        this.psiSessionRepository = psiSessionRepository;
        this.psiKeyRepository = psiKeyRepository;
        this.storedAlgorithmKey = storedAlgorithmKey;
        this.psiCacheProviderImplementation = psiCacheProviderImplementation;
    }

    public Instant getExpirationTime(){
        return Instant.now().minus(minutesBeforeSessionExpiration, ChronoUnit.MINUTES);
    }

    public PsiSessionWrapperDTO initSession(PsiAlgorithmParameterDTO sessionParameterDTO) {
        // fulfill PsiAlgorithmParameterDTO
        PsiAlgorithmParameterDTO psiAlgorithmParameterDTO = new PsiAlgorithmParameterDTO();
        psiAlgorithmParameterDTO.setAlgorithm(sessionParameterDTO.getAlgorithm());
        psiAlgorithmParameterDTO.setKeySize(sessionParameterDTO.getKeySize());

        // Retrieve the key corresponding to the pair <algorithm, keySIze>
        PsiKey psiKey = storedAlgorithmKey.get(
                AlgorithmMapper.toEntity(sessionParameterDTO.getAlgorithm()),
                sessionParameterDTO.getKeySize())
                .orElseThrow(KeyNotAvailableException::new);

        // Build ServerKeyDescription
        PsiServerKeyDescription psiServerKeyDescription;
        //TODO: abbiamo un metodo che la genera passandogli le info necessarie?
        switch(sessionParameterDTO.getAlgorithm()){
            case BS:
                BsPsiServerKeyDescription bsServerKeyDescription = new BsPsiServerKeyDescription();
                bsServerKeyDescription.setModulus(psiKey.getModulus());
                bsServerKeyDescription.setPrivateKey(psiKey.getPrivateKey());
                bsServerKeyDescription.setPublicKey(psiKey.getPublicKey());
                bsServerKeyDescription.setKeyId(psiKey.getKeyId());
                psiServerKeyDescription = bsServerKeyDescription;
                break;
            case DH:
                //TODO
            default:
                throw new AlgorithmNotSupportedException();
        }

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
                SessionDtoMapper.getSessionDtoFromServerSession(psiServerSession));

        return psiSessionWrapperDTO;
    }

    PsiServer loadPsiServerBySessionId(long sessionId) throws SessionNotFoundException, SessionExpiredException {
        PsiServerSession psiServerSession = getPsiServerSession(sessionId);
        return PsiServerFactory.loadSession(psiServerSession, psiCacheProviderImplementation);
    }

    //TODO: spostare questo metodo nella libreria? (magari già c'è) Far diventare gli algoritmi un enum?
    private static PsiServerSession buildPsiServerSession(Algorithm algorithm, int keySize, String modulus, String privateKey){
        PsiServerSession psiServerSession;
        switch(algorithm){
            case BS:
                BsPsiServerSession bsServerSession = new BsPsiServerSession();
                bsServerSession.setAlgorithm("BS");
                bsServerSession.setKeySize(keySize);
                bsServerSession.setModulus(modulus);
                bsServerSession.setServerPrivateKey(privateKey);
                bsServerSession.setCacheEnabled(true); //TODO, dipendentemente dalla presenza di una keyId
                psiServerSession = bsServerSession;
                break;
            case DH:
                //TODO
            default:
                throw new AlgorithmNotSupportedException();
        }
        return psiServerSession;
    }

    private PsiServerSession getPsiServerSession(long sessionId) throws SessionNotFoundException, SessionExpiredException {
        PsiSession psiSession = psiSessionRepository.findById(sessionId)
                .orElseThrow(SessionNotFoundException::new);
        if(psiSession.getExpiration().isBefore(Instant.now()))
            throw new SessionExpiredException();
        PsiKey psiKey = null;
        if(psiSession.getKeyId() != null)
            psiKey = psiKeyRepository.findByKeyId(psiSession.getKeyId())
                    .orElseThrow(KeyNotAvailableException::new);

        return buildPsiServerSession(psiSession.getAlgorithm(), psiSession.getKeySize(), psiKey.getModulus(), psiKey.getPrivateKey());
    }

    public PsiSessionWrapperDTO getPsiSessionWrapperDTO(long sessionId) throws SessionNotFoundException {
        PsiSession psiSession = psiSessionRepository.findById(sessionId)
                .orElseThrow(SessionNotFoundException::new);
        PsiKey psiKey = null;
        if(psiSession.getKeyId() != null)
            psiKey = psiKeyRepository.findByKeyId(psiSession.getKeyId())
                    .orElseThrow(KeyNotAvailableException::new);

        PsiServerSession psiServerSession =
                buildPsiServerSession(psiSession.getAlgorithm(), psiSession.getKeySize(), psiKey.getModulus(), psiKey.getPrivateKey());

        PsiSessionWrapperDTO psiSessionWrapperDTO = new PsiSessionWrapperDTO();
        psiSessionWrapperDTO.setExpiration(psiSession.getExpiration());
        psiSessionWrapperDTO.setSessionId(psiSession.getId());
        psiSessionWrapperDTO.setPsiSessionDTO(
                SessionDtoMapper.getSessionDtoFromServerSession(psiServerSession));

        return psiSessionWrapperDTO;
    }



}
