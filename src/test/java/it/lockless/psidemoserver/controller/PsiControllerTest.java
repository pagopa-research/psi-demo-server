package it.lockless.psidemoserver.controller;

import it.lockless.psidemoserver.entity.PsiElement;
import it.lockless.psidemoserver.model.PsiDatasetMapDTO;
import it.lockless.psidemoserver.model.PsiServerDatasetPageDTO;
import it.lockless.psidemoserver.model.PsiSessionWrapperDTO;
import it.lockless.psidemoserver.repository.PsiElementRepository;
import it.lockless.psidemoserver.service.cache.RedisPsiCacheProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import psi.client.PsiClient;
import psi.client.PsiClientFactory;
import psi.dto.PsiAlgorithmParameterDTO;
import psi.model.PsiAlgorithm;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PsiControllerTest {

	@Autowired
	private PsiElementRepository psiElementRepository;

	@Autowired
	private PsiController controller;

	@Autowired(required = false)
	private RedisPsiCacheProvider cacheImplementation;

	@BeforeEach
	void setup() {
		int matchingElements = 10;
		int mismatchingElements = 20;
		List<PsiElement> psiElementList = new ArrayList<>(matchingElements+mismatchingElements);
		PsiElement psiElement;
		for(int i = 0; i< matchingElements; i++) {
			psiElement = new PsiElement();
			psiElement.setValue("MATCHING-"+i);
			psiElementList.add(psiElement);
		}
		for(int i = 0; i< mismatchingElements; i++) {
			psiElement = new PsiElement();
			psiElement.setValue("SERVER-"+(matchingElements+i));
			psiElementList.add(psiElement);
		}
		psiElementRepository.saveAll(psiElementList);
	}

	@Test
	void fullExecutionTest() {
		// Retrieve the list of available algorithms and relative keySize
		List<PsiAlgorithmParameterDTO> sessionParameterDTOList = controller.getParameters().getBody().getContent();
		assertNotNull(sessionParameterDTOList);
		sessionParameterDTOList.forEach(dto -> assertTrue(Arrays.asList(PsiAlgorithm.values()).contains(dto.getAlgorithm())));

		PsiAlgorithmParameterDTO sessionParameterDTO = sessionParameterDTOList.get(0);

		// Initialize a new Session
		PsiSessionWrapperDTO psiSessionWrapperDTO = controller.initSession(sessionParameterDTO).getBody();
		assertNotNull(psiSessionWrapperDTO);
		assertNotNull(psiSessionWrapperDTO.getPsiSessionDTO());
		assertNotNull(psiSessionWrapperDTO.getExpiration());
		assertEquals(sessionParameterDTO, psiSessionWrapperDTO.getPsiSessionDTO().getPsiAlgorithmParameterDTO());
		Long sessionId = psiSessionWrapperDTO.getSessionId();

		// CLIENT SIDE: Setup client
		PsiClient psiClient = PsiClientFactory.loadSession(psiSessionWrapperDTO.getPsiSessionDTO());

		// CLIENT SIDE: Building and encrypting client dataset
		Set<String> clientDataset = new HashSet<>(1500);
		for(int i = 0 ; i < 10; i++) clientDataset.add("MATCHING-"+i);
		for(int i = 10 ; i < 15; i++) clientDataset.add("CLIENT-"+i);

		Map<Long, String> encryptedClientDataset = psiClient.loadAndEncryptClientDataset(clientDataset);
		PsiDatasetMapDTO psiDatasetMapDTO = new PsiDatasetMapDTO(encryptedClientDataset);

		// Double encrypt client dataset
		PsiDatasetMapDTO returnedPsiDatasetMapDTO = controller.encryptClientDataset(sessionId, psiDatasetMapDTO).getBody();
		assertNotNull(returnedPsiDatasetMapDTO);
		assertEquals(clientDataset.size(), returnedPsiDatasetMapDTO.getContent().size());

		// Encrypt server dataset in two different pages
		PsiServerDatasetPageDTO page = controller.getEncryptedServerServerDataset(sessionId, 0, 20).getBody();
		assertNotNull(page);
		assertEquals(30,page.getTotalEntries());
		assertEquals(2,page.getTotalPages());
		assertEquals(20,page.getEntries());
		assertEquals(20,page.getSize());
		Set<String> encryptedServerDataset = new HashSet<>(page.getContent());
		page = controller.getEncryptedServerServerDataset(sessionId, 1, 20).getBody();
		assertNotNull(page);
		assertEquals(10,page.getEntries());
		assertEquals(20,page.getSize());
		encryptedServerDataset.addAll(page.getContent());
		assertEquals(30, encryptedServerDataset.size());

		// CLIENT SIDE: load server encrypted datasets
		psiClient.loadServerDataset(encryptedServerDataset);
		psiClient.loadDoubleEncryptedClientDataset(returnedPsiDatasetMapDTO.getContent());

		// CLIENT SIDE: compute psi
		Set<String> psiSet = psiClient.computePsi();
		assertEquals(10, psiSet.size());
		psiSet.forEach(elem -> assertTrue(elem.startsWith("MATCHING-")));

	}

}
