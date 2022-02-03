package it.lockless.psidemoserver.service;

import it.lockless.psidemoserver.repository.PsiElementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class DatasetServiceTest {

	@Autowired
	private PsiElementRepository psiElementRepository;

	@Autowired
	private DatasetService datasetService;

	@BeforeEach
	void setup() {

	}

	@Test
	void test() {
		Map<String, Integer> map = new HashMap<>();
		map.put("ONE", 10);
		map.put("TWO", 20);
		map.put("THREE", 30);

		datasetService.initServerDataset(map);

		assertEquals(60, psiElementRepository.count());
	}

}
