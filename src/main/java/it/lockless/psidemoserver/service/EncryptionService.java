package it.lockless.psidemoserver.service;

import it.lockless.psidemoserver.entity.PsiElement;
import it.lockless.psidemoserver.entity.enumeration.Algorithm;
import it.lockless.psidemoserver.mapper.AlgorithmMapper;
import it.lockless.psidemoserver.model.PsiAlgorithmParameterListDTO;
import it.lockless.psidemoserver.model.PsiDatasetMapDTO;
import it.lockless.psidemoserver.model.PsiServerDatasetPageDTO;
import it.lockless.psidemoserver.repository.PsiElementRepository;
import it.lockless.psidemoserver.util.exception.SessionExpiredException;
import it.lockless.psidemoserver.util.exception.SessionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import psi.model.PsiAlgorithmParameter;
import psi.server.PsiServer;

import java.util.*;

/**
 * It offers functionalities related to dataset encryptions
 */

@Service
public class EncryptionService {

    private static final Logger log = LoggerFactory.getLogger(EncryptionService.class);

    private final PsiSessionService psiSessionService;

    private final PsiElementRepository psiElementRepository;

    public EncryptionService(PsiSessionService psiSessionService, PsiElementRepository psiElementRepository) {
        this.psiSessionService = psiSessionService;
        this.psiElementRepository = psiElementRepository;
    }

    /**
     * Retrieve locally a list of supported algorithms and corresponding key sizes
     * Note: this list should be retrieved directly from the sdk,
     * but we want offer a smaller set respect the one supported by the sdk itself
     */
    public PsiAlgorithmParameterListDTO getAvailablePsiAlgorithmParameter(){
        log.debug("Calling getAvailableSessionParameterDTO");

        // Depending on the algorithms and keySize supported, populate a list if PsiAlgorithmParameter
        List<PsiAlgorithmParameter> psiAlgorithmParameterList = new LinkedList<>();
        for (Algorithm algorithm : Algorithm.values()) {
            for (int keySize : algorithm.getSupportedKeySize())
                psiAlgorithmParameterList.add(
                        new PsiAlgorithmParameter(AlgorithmMapper.toPsiAlgorithm(algorithm), keySize));
        }
        return new PsiAlgorithmParameterListDTO(psiAlgorithmParameterList);
    }

    /**
     * Encrypt the passed client dataset using the key associated with the session
     */
    public PsiDatasetMapDTO encryptClientSet(long sessionId, PsiDatasetMapDTO clientSet) throws SessionNotFoundException, SessionExpiredException {
        log.debug("Calling encryptClientSet with sessionId = {}, clientSet.size() = {}", sessionId, clientSet.getContent().size());
        // Retrieve psiServe instance
        PsiServer psiServer = psiSessionService.loadPsiServerBySessionId(sessionId);

        // Encrypt client dataset
        Map<Long, String> encryptedClientSet = psiServer.encryptDatasetMap(clientSet.getContent());

        // Build response
        return new PsiDatasetMapDTO(encryptedClientSet);
    }

    /**
     * Encrypt a server dataset page (represented by page number and size) using the key associated with the session
     */
    public PsiServerDatasetPageDTO getEncryptedServerDataset(long sessionId, int page, int size) throws SessionNotFoundException, SessionExpiredException {
        log.debug("Calling getEncryptedServerDataset with sessionId = {}, page = {}, size = {}", sessionId, page, size);
        // Retrieve the psiServe instance
        PsiServer psiServer = psiSessionService.loadPsiServerBySessionId(sessionId);

        // Retrieve a clear page depending on the specified page and size
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
        psiServerDatasetPageDTO.setLast((page + 1) >= psiElementPage.getTotalPages());

        return psiServerDatasetPageDTO;
    }
}
