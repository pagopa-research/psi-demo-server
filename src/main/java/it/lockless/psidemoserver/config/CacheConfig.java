package it.lockless.psidemoserver.config;

import it.lockless.psidemoserver.service.cache.PsiCacheProviderImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

// Important: this is only a mock of a real keyStore and it is not intended to be used in a real environment.

@Component
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    public PsiCacheProviderImplementation createCache() {
        // TODO: connect to redis
        return new PsiCacheProviderImplementation();
    }
}
