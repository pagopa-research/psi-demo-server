package it.lockless.psidemoserver;

import it.lockless.psidemoserver.config.StoredAlgorithmKey;
import it.lockless.psidemoserver.entity.PsiElement;
import it.lockless.psidemoserver.model.enumeration.AlgorithmDTO;
import it.lockless.psidemoserver.repository.PsiElementRepository;
import it.lockless.psidemoserver.entity.enumeration.Algorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PsiDemoServerApplicationTests {

	@Autowired
	PsiElementRepository psiElementRepository;

	@Autowired
	StoredAlgorithmKey supportedAlgorithms;

	@Test
	void contextLoads() {
		int elements = 1000;
		List<PsiElement> psiElementList = new ArrayList<>(elements);
		for(int i = 0; i< elements; i++) {
			PsiElement psiElement = new PsiElement();
			psiElement.setValue("element-"+i);
			psiElementList.add(psiElement);
		}
		psiElementRepository.saveAll(psiElementList);

		assertEquals(1000, psiElementRepository.count());
	}

	@Test
	void secretConfigText(){

		for(AlgorithmDTO algorithm: Algorithm.values())
			for(int size : algorithm.getSupportedKeySize())
				if(supportedAlgorithms.get(algorithm,size).isPresent())
					System.out.println(algorithm+" - "+size+" - "+supportedAlgorithms.get(algorithm,size).get().getModulus());

		System.out.println();

		for(AlgorithmDTO algorithm: Algorithm.values())
			for(int size : algorithm.getSupportedKeySize())
				if(supportedAlgorithms.get(algorithm,size).isPresent())
					System.out.println(algorithm+" - "+size+" - "+supportedAlgorithms.get(algorithm,size).get().getModulus());
	}

}
