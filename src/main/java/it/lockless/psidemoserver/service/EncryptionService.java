package it.lockless.psidemoserver.service;

import it.lockless.psidemoserver.entity.PsiElement;
import it.lockless.psidemoserver.entity.enumeration.Algorithm;
import it.lockless.psidemoserver.mapper.AlgorithmMapper;
import it.lockless.psidemoserver.model.PsiDatasetMapDTO;
import it.lockless.psidemoserver.model.PsiServerDatasetPageDTO;
import it.lockless.psidemoserver.model.SessionParameterDTO;
import it.lockless.psidemoserver.repository.PsiElementRepository;
import it.lockless.psidemoserver.util.exception.SessionExpiredException;
import it.lockless.psidemoserver.util.exception.SessionNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import psi.server.PsiServer;

import java.util.*;

@Service
public class EncryptionService {

    private final PsiSessionService psiSessionService;

    private final PsiElementRepository psiElementRepository;

    public EncryptionService(PsiSessionService psiSessionService, PsiElementRepository psiElementRepository) {
        this.psiSessionService = psiSessionService;
        this.psiElementRepository = psiElementRepository;
    }

    public List<SessionParameterDTO> getAvailableSessionParameterDTO(){
        List<SessionParameterDTO> sessionParameterDTOList = new LinkedList<>();
        for (Algorithm algorithm : Algorithm.values())
            for (int keySize : algorithm.getSupportedKeySize())
                sessionParameterDTOList.add(
                        new SessionParameterDTO(AlgorithmMapper.toDTO(algorithm), keySize));

        return sessionParameterDTOList;
    }

    public PsiDatasetMapDTO encryptClientSet(long sessionId, PsiDatasetMapDTO clientSet) throws SessionNotFoundException, SessionExpiredException {
        // Retrieve psiServe instance
        PsiServer psiServer = psiSessionService.loadPsiServerBySessionId(sessionId);

        // Encrypt client dataset
        Map<Long, String> encryptedClientSet = psiServer.encryptDatasetMap(clientSet.getContent());

        // Build response
        return new PsiDatasetMapDTO(encryptedClientSet);
    }

    public PsiServerDatasetPageDTO getEncryptedServerDataset(long sessionId, int page, int size) throws SessionNotFoundException, SessionExpiredException {
        // Retrieve psiServe instance
        PsiServer psiServer = psiSessionService.loadPsiServerBySessionId(sessionId);

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
