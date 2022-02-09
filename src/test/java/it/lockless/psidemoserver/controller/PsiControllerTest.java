package it.lockless.psidemoserver.controller;

import it.lockless.psidemoserver.entity.PsiElement;
import it.lockless.psidemoserver.model.*;
import it.lockless.psidemoserver.repository.PsiElementRepository;
import it.lockless.psidemoserver.service.EncryptionService;
import it.lockless.psidemoserver.service.PsiSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;
import psi.PsiClientFactory;
import psi.cache.PsiCacheProvider;
import psi.client.PsiClient;
import psi.client.PsiClientKeyDescription;
import psi.exception.UnsupportedKeySizeException;
import psi.model.PsiAlgorithm;
import psi.model.PsiAlgorithmParameter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PsiControllerTest {

	@Autowired
	private PsiElementRepository psiElementRepository;

	@Autowired
	private PsiController controller;

	@Autowired
	private PsiSessionService psiSessionService;

	@Autowired
	private EncryptionService encryptionService;

	@Mock
	private PsiCacheProvider psiCacheProvider;

	private PsiCacheProvider clientPsiCacheProvider = new LocalCacheImplementation();

	private ConcurrentHashMap<String, String> cacheMock;

	private AtomicInteger cachePut;
	private AtomicInteger cacheHit;
	private AtomicInteger cacheMiss;

	@BeforeEach
	void setup() {
		when(psiCacheProvider.get(any())).thenAnswer(invocation -> {
			String key = (String) invocation.getArguments()[0];
			String ret = cacheMock.get(key);
			if (ret == null){
				cacheMiss.incrementAndGet();
				return Optional.empty();
			}
			cacheHit.incrementAndGet();
			return Optional.of(ret);
		});

		doAnswer(invocation -> {
			String key = invocation.getArgument(0);
			String value = invocation.getArgument(1);
			cachePut.incrementAndGet();
			cacheMock.put(key,value);
			return null;
		}).when(psiCacheProvider).put(any(), any());

		ReflectionTestUtils.setField(psiSessionService, "psiCacheProvider",psiCacheProvider);
		ReflectionTestUtils.setField(encryptionService, "psiSessionService",psiSessionService);
		ReflectionTestUtils.setField(controller, "encryptionService",encryptionService);
		ReflectionTestUtils.setField(controller, "psiSessionService",psiSessionService);
	}

	private void setupServerDataset(int totalElements, int matchingElements){
		int mismatchingElements = totalElements - matchingElements;
		List<PsiElement> psiElementList = new ArrayList<>(totalElements);
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

	private Set<String> setupClientDataset(int totalElements, int matchingElements){
		int mismatchingElements = totalElements - matchingElements;
		Set<String> clientDataset = new HashSet<>(totalElements);
		for (int i = 0; i < matchingElements; i++)
			clientDataset.add("MATCHING-" + i);
		for (int i = 0; i < mismatchingElements; i++)
			clientDataset.add("CLIENT-" + i);
		return clientDataset;
	}

	@Test
	void fullExecutionTest() throws UnsupportedKeySizeException {
		int serverTotalElements = 30;
		int clientTotalElements = 20;
		int matchingElements = 10;

		// Building server and client dataset
		setupServerDataset(serverTotalElements, matchingElements);
		Set<String> clientDataset = setupClientDataset(clientTotalElements, matchingElements);

		// Retrieve the list of available algorithms and relative keySize
		PsiAlgorithmParameterListDTO psiAlgorithmParameterListDTO = controller.getParameters().getBody();
		assertNotNull(psiAlgorithmParameterListDTO);
		List<PsiAlgorithmParameter> sessionParameterList = psiAlgorithmParameterListDTO.getContent();
		assertNotNull(sessionParameterList);
		assertEquals(8, sessionParameterList.size());
		sessionParameterList.forEach(dto -> assertTrue(Arrays.asList(PsiAlgorithm.values()).contains(dto.getAlgorithm())));

		for(PsiAlgorithmParameter psiAlgorithmParameter : sessionParameterList) {
			PsiClientKeyDescription psiClientKeyDescription = null;

			// Initialize a new Session
			PsiClientSessionDTO psiClientSessionDTO = (PsiClientSessionDTO) controller.initSession(new PsiAlgorithmParameterDTO(psiAlgorithmParameter)).getBody();
			assertNotNull(psiClientSessionDTO);
			assertNotNull(psiClientSessionDTO.getPsiClientSession());
			assertNotNull(psiClientSessionDTO.getExpiration());
			assertEquals(psiAlgorithmParameter, psiClientSessionDTO.getPsiClientSession().getPsiAlgorithmParameter());
			Long sessionId = psiClientSessionDTO.getSessionId();

			// Reset cache content
			cacheMock = new ConcurrentHashMap<>();

			// The whole execution is performed two times to verify that the cache is correctly used
			for (int i = 0 ; i < 2 ;  i++) {
				// Reset cache control variable
				cacheHit = new AtomicInteger(0);
				cacheMiss = new AtomicInteger(0);
				cachePut = new AtomicInteger(0);

				// CLIENT SIDE: Setup client
				// Note: the client cache is used to enhance the server cache while using random number based algorithms
				PsiClient psiClient;
				if (psiClientKeyDescription == null) {
					psiClient = PsiClientFactory.loadSession(psiClientSessionDTO.getPsiClientSession(), clientPsiCacheProvider);
					psiClientKeyDescription = psiClient.getClientKeyDescription();
				} else {
					// During the second loop we use the same key
					psiClient = PsiClientFactory.loadSession(psiClientSessionDTO.getPsiClientSession(), psiClientKeyDescription, clientPsiCacheProvider);
				}

				// CLIENT SIDE: encrypting client dataset
				Map<Long, String> encryptedClientDataset = psiClient.loadAndEncryptClientDataset(clientDataset);
				PsiDatasetMapDTO psiDatasetMapDTO = new PsiDatasetMapDTO(encryptedClientDataset);

				// Double encrypt client dataset
				PsiDatasetMapDTO returnedPsiDatasetMapDTO = (PsiDatasetMapDTO) controller.encryptClientDataset(sessionId, psiDatasetMapDTO).getBody();
				assertNotNull(returnedPsiDatasetMapDTO);
				assertEquals(clientDataset.size(), returnedPsiDatasetMapDTO.getContent().size());

				// Encrypt server dataset in two different pages
				PsiServerDatasetPageDTO page = (PsiServerDatasetPageDTO) controller.getEncryptedServerServerDataset(sessionId, 0, 20).getBody();
				assertNotNull(page);
				assertEquals(serverTotalElements, page.getTotalEntries());
				assertEquals(2, page.getTotalPages());
				assertEquals(20, page.getEntries());
				assertEquals(20, page.getSize());

				Set<String> encryptedServerDataset = new HashSet<>(page.getContent());
				page = (PsiServerDatasetPageDTO) controller.getEncryptedServerServerDataset(sessionId, 1, 20).getBody();
				assertNotNull(page);
				assertEquals(10, page.getEntries());
				assertEquals(20, page.getSize());
				encryptedServerDataset.addAll(page.getContent());
				assertEquals(30, encryptedServerDataset.size());

				if (i == 0){
					// During the first execution the only cacheHits are the one linked to the keyId retrieving
					assertEquals(2, cacheHit.get()); // related to the two keyDescription retrieving during the initSession, after the first one
					assertEquals(serverTotalElements+clientTotalElements + 1, cacheMiss.get()); // first keyStoring + server dataset + client dataset
					assertEquals(serverTotalElements+clientTotalElements + 1, cachePut.get()); // first keyStoring + server dataset + client dataset
				} else {
					// During the second execution we have not cacheMiss/cachePut since we are working on the same dataset of the first execution
					assertEquals(serverTotalElements+clientTotalElements + 3, cacheHit.get()); // 3 keyStoring + server dataset + client dataset
					assertEquals(0, cacheMiss.get());
					assertEquals(0, cachePut.get());
				}

				// CLIENT SIDE: load server encrypted datasets
				psiClient.loadAndProcessServerDataset(encryptedServerDataset);
				psiClient.loadDoubleEncryptedClientDataset(returnedPsiDatasetMapDTO.getContent());

				// CLIENT SIDE: compute psi
				Set<String> psiSet = psiClient.computePsi();
				assertEquals(matchingElements, psiSet.size());
				psiSet.forEach(elem -> assertTrue(elem.startsWith("MATCHING-")));
			}

		}
	}

	private static class LocalCacheImplementation implements PsiCacheProvider {

		private ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

		@Override
		public Optional<String> get(String key) {
			String value = cache.get(key);
			if (value == null)
				return Optional.empty();
			return Optional.of(value);
		}

		@Override
		public void put(String key, String value) {
			cache.put(key, value);
		}
	}
}
