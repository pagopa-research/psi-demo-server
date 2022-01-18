package it.lockless.psidemoserver.service;

import it.lockless.psidemoserver.config.StoredAlgorithmKey;
import it.lockless.psidemoserver.entity.PsiElement;
import it.lockless.psidemoserver.entity.PsiKey;
import it.lockless.psidemoserver.entity.PsiSession;
import it.lockless.psidemoserver.entity.enumeration.Algorithm;
import it.lockless.psidemoserver.mapper.AlgorithmMapper;
import it.lockless.psidemoserver.model.PsiServerDatasetPageDTO;
import it.lockless.psidemoserver.model.PsiSessionWrapperDTO;
import it.lockless.psidemoserver.model.SessionParameterDTO;
import it.lockless.psidemoserver.model.PsiDatasetMapDTO;
import it.lockless.psidemoserver.repository.PsiElementRepository;
import it.lockless.psidemoserver.repository.PsiKeyRepository;
import it.lockless.psidemoserver.repository.PsiSessionRepository;
import it.lockless.psidemoserver.util.exception.AlgorithmNotSupportedException;
import it.lockless.psidemoserver.util.exception.KeyNotAvailableException;
import it.lockless.psidemoserver.util.exception.SessionExpiredException;
import it.lockless.psidemoserver.util.exception.SessionNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import java.util.*;

public class PsiSessionService {

    @Value("session.expiration.minutes")
    private int minutesBeforeSessionExpiration;

    private final PsiSessionRepository psiSessionRepository;

    private final PsiKeyRepository psiKeyRepository;

    private final PsiElementRepository psiElementRepository;

    private final StoredAlgorithmKey storedAlgorithmKey;

    public PsiSessionService(PsiSessionRepository psiSessionRepository, PsiKeyRepository psiKeyRepository, PsiElementRepository psiElementRepository, StoredAlgorithmKey storedAlgorithmKey) {
        this.psiSessionRepository = psiSessionRepository;
        this.psiKeyRepository = psiKeyRepository;
        this.psiElementRepository = psiElementRepository;
        this.storedAlgorithmKey = storedAlgorithmKey;
    }

    public Instant getExpirationTime(){
        return Instant.now().minus(minutesBeforeSessionExpiration, ChronoUnit.MINUTES);
    }

    public List<SessionParameterDTO> getAvailableSessionParameterDTO(){
        List<SessionParameterDTO> sessionParameterDTOList = new LinkedList<>();
        for (Algorithm algorithm : Algorithm.values())
            for (int keySize : algorithm.getSupportedKeySize())
                sessionParameterDTOList.add(
                        new SessionParameterDTO(AlgorithmMapper.toDTO(algorithm), keySize));

            return sessionParameterDTOList;
    }

    public PsiSessionWrapperDTO initSession(SessionParameterDTO sessionParameterDTO) {
        PsiAlgorithmParameterDTO psiAlgorithmParameterDTO = new PsiAlgorithmParameterDTO();
        psiAlgorithmParameterDTO.setAlgorithm(sessionParameterDTO.getAlgorithm().toString());
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
            case RSA:
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
        PsiServerSession psiServerSession = PsiServerFactory.initSession(psiAlgorithmParameterDTO, psiServerKeyDescription);

        PsiSession psiSession = new PsiSession();
        psiSession.setCacheEnabled(false);
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
    
    private PsiServerSession getPsiServerSession(long sessionId) throws SessionNotFoundException, SessionExpiredException {
        PsiSession psiSession = psiSessionRepository.findById(sessionId)
                .orElseThrow(SessionNotFoundException::new);
        if(psiSession.getExpiration().isBefore(Instant.now()))
            throw new SessionExpiredException();
        PsiKey psiKey = null;
        if(psiSession.getKeyId() != null)
            psiKey = psiKeyRepository.findByKeyId(psiSession.getKeyId())
                    .orElseThrow(KeyNotAvailableException::new);

        PsiServerSession psiServerSession;
        switch(psiSession.getAlgorithm()){
            case RSA:
                BsPsiServerSession bsServerSession = new BsPsiServerSession();
                bsServerSession.setAlgorithm("BS");
                bsServerSession.setKeySize(psiSession.getKeySize());
                bsServerSession.setKeySize(psiSession.getKeySize());
                bsServerSession.setModulus(psiKey.getModulus());
                bsServerSession.setServerPrivateKey(psiKey.getPrivateKey());
                bsServerSession.setCacheEnabled(false); //TODO, dipendentemente dalla presenza di una keyId
                psiServerSession = bsServerSession;
                break;
            case DH:
                //TODO
            default:
                throw new AlgorithmNotSupportedException();
        }
        return psiServerSession;
    }

    public PsiServer loadPsiServerBySessionId(long sessionId) throws SessionNotFoundException, SessionExpiredException {
        PsiServerSession psiServerSession = getPsiServerSession(sessionId);
        return PsiServerFactory.loadSession(psiServerSession);
    }

    public PsiDatasetMapDTO encryptClientSet(long sessionId, PsiDatasetMapDTO clientSet) throws SessionNotFoundException, SessionExpiredException {
        // Retrieve psiServe instance
        PsiServer psiServer = loadPsiServerBySessionId(sessionId);

        // Encrypt client dataset
        Map<Long, String> encryptedClientSet = psiServer.encryptDatasetMap(clientSet.getContent());

        // Build response
        return new PsiDatasetMapDTO(encryptedClientSet);
    }

    public PsiServerDatasetPageDTO getEncryptedServerDataset(long sessionId, int page, int size) throws SessionNotFoundException, SessionExpiredException {
        // Retrieve psiServe instance
        PsiServer psiServer = loadPsiServerBySessionId(sessionId);

        // Retrieve clear page
        Page<PsiElement> psiElementPage = psiElementRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
        Set<String> clearElementList = new HashSet<>(size);
        psiElementPage.iterator().forEachRemaining(element -> clearElementList.add(element.getValue()));

        // Encrypt and build response
        PsiServerDatasetPageDTO psiServerDatasetPageDTO = new PsiServerDatasetPageDTO();
        psiServerDatasetPageDTO.setContent(psiServer.encryptDataset(clearElementList));
        psiServerDatasetPageDTO.setSize(size);
        psiServerDatasetPageDTO.setTotalEntries(psiElementPage.getTotalElements());
        psiServerDatasetPageDTO.setTotalPages(psiElementPage.getTotalPages());
        psiServerDatasetPageDTO.setPage(page);
        psiServerDatasetPageDTO.setEntries(clearElementList.size());
        psiServerDatasetPageDTO.setLast(page >= psiElementPage.getTotalPages());

        return psiServerDatasetPageDTO;
    }
}
