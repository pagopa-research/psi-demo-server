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
 * Offers the functionalities for PSI calculation by calling the PSI-SDK.
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
     * Retrieves locally a list of supported algorithms and corresponding key sizes
     * Note: this list should be retrieved directly from the sdk,
     * but we want offer a smaller set respect the one supported by the sdk itself
     * @return a PsiAlgorithmParameterListDTO containing a list of PsiAlgorithmParameter
     */
    public PsiAlgorithmParameterListDTO getAvailablePsiAlgorithmParameter(){
        log.debug("Calling getAvailableSessionParameterDTO");

        // Depending on the algorithms and keySize supported, populates a list if PsiAlgorithmParameter
        List<PsiAlgorithmParameter> psiAlgorithmParameterList = new LinkedList<>();
        for (Algorithm algorithm : Algorithm.values()) {
            for (int keySize : algorithm.getSupportedKeySize())
                psiAlgorithmParameterList.add(
                        new PsiAlgorithmParameter(AlgorithmMapper.toPsiAlgorithm(algorithm), keySize));
        }
        return new PsiAlgorithmParameterListDTO(psiAlgorithmParameterList);
    }

    /**
     * Encrypts the passed client dataset using the key associated with the session.
     * @param sessionId the id identifying the session associated to the client
     * @param clientSet the client set to be encrypted by the server
     * @return a PsiDatasetMapDTO containing the client encrypted dataset
     */
    public PsiDatasetMapDTO encryptClientSet(long sessionId, PsiDatasetMapDTO clientSet) throws SessionNotFoundException, SessionExpiredException {
        log.debug("Calling encryptClientSet with sessionId = {}, clientSet.size() = {}", sessionId, clientSet.getContent().size());
        // Retrieves psiServe instance
        PsiServer psiServer = psiSessionService.loadPsiServerBySessionId(sessionId);

        // Encrypts client dataset
        Map<Long, String> encryptedClientSet = psiServer.encryptDatasetMap(clientSet.getContent());

        // Builds response
        return new PsiDatasetMapDTO(encryptedClientSet);
    }

    /**
     * Encrypts a server dataset page (represented by page number and size) using the key associated with the session.
     * @param sessionId the id identifying the session associated to the client
     * @param page		the page to be retrieved by the server set
     * @param size		the size of the page to be retrieved
     * @return 	a PsiServerDatasetPageDTO containing the server encrypted dataset page
     */
    public PsiServerDatasetPageDTO getEncryptedServerDataset(long sessionId, int page, int size) throws SessionNotFoundException, SessionExpiredException {
        log.debug("Calling getEncryptedServerDataset with sessionId = {}, page = {}, size = {}", sessionId, page, size);
        // Retrieves the psiServe instance
        PsiServer psiServer = psiSessionService.loadPsiServerBySessionId(sessionId);

        // Retrieves a clear page depending on the specified page and size
        Page<PsiElement> psiElementPage = psiElementRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
        Set<String> clearElementList = new HashSet<>(size);
        psiElementPage.iterator().forEachRemaining(element -> clearElementList.add(element.getValue()));

        // Encrypts and build response
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
