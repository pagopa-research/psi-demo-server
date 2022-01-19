package it.lockless.psidemoserver.controller;

import it.lockless.psidemoserver.config.StoredAlgorithmKey;
import it.lockless.psidemoserver.entity.PsiElement;
import it.lockless.psidemoserver.entity.enumeration.Algorithm;
import it.lockless.psidemoserver.model.PsiSessionWrapperDTO;
import it.lockless.psidemoserver.repository.PsiElementRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.bind.annotation.RequestBody;
import psi.dto.PsiAlgorithmDTO;
import psi.dto.PsiAlgorithmParameterDTO;

import javax.validation.constraints.AssertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PsiControllerTest {

	@Autowired
	PsiElementRepository psiElementRepository;

	@Autowired
	PsiController controller;



	@BeforeEach
	void setup() {
		int elements = 1000;
		List<PsiElement> psiElementList = new ArrayList<>(elements);
		for(int i = 0; i< elements; i++) {
			PsiElement psiElement = new PsiElement();
			psiElement.setValue("element-"+i);
			psiElementList.add(psiElement);
		}
		psiElementRepository.saveAll(psiElementList);
	}

	@Test
	void fullExecutionTest() {
		// Retrieve the list of available algorithms and relative keySize
		List<PsiAlgorithmParameterDTO> sessionParameterDTOList = controller.getParameters().getBody();
		assertNotNull(sessionParameterDTOList);
		sessionParameterDTOList.forEach(dto -> {
			assertTrue(Arrays.asList(PsiAlgorithmDTO.values()).contains(dto.getAlgorithm()));
		});

		PsiAlgorithmParameterDTO sessionParameterDTO = sessionParameterDTOList.get(0);

		// Initialize a new Session
		PsiSessionWrapperDTO psiSessionWrapperDTO = controller.initSession(sessionParameterDTO).getBody();
		assertNotNull(psiSessionWrapperDTO);
		assertNotNull(psiSessionWrapperDTO.getPsiSessionDTO());
		assertNotNull(psiSessionWrapperDTO.getExpiration());
		assertEquals(sessionParameterDTO, psiSessionWrapperDTO.getPsiSessionDTO().getSessionParameterDTO());
		System.out.println(psiSessionWrapperDTO);
	}

}
