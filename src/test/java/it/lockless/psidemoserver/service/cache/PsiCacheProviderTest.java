package it.lockless.psidemoserver.service.cache;

import it.lockless.psidemoserver.util.exception.CacheKeyAlreadyWrittenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import psi.cache.PsiCacheProvider;

import java.security.SecureRandom;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PsiCacheProviderTest {

	@Autowired
	private PsiCacheProvider psiCacheProvider;

	@BeforeEach
	void setup() {

	}

	@Test
	void cacheTest() {
		if (psiCacheProvider != null) {
			String key = "key" + (new SecureRandom()).nextLong();

			psiCacheProvider.put(key, "value");
			assertEquals(Optional.of("value"), psiCacheProvider.get(key));
			assertEquals(Optional.empty(), psiCacheProvider.get("notAKey"));

			assertThrows(CacheKeyAlreadyWrittenException.class, () -> {
				psiCacheProvider.put(key, "value2");
			});
		}
	}

}
