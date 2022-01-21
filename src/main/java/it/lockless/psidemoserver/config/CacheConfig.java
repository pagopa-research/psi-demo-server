package it.lockless.psidemoserver.config;

import it.lockless.psidemoserver.service.cache.RedisPsiCacheProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import psi.cache.PsiCacheProvider;
import redis.clients.jedis.exceptions.JedisConnectionException;

// Important: this is only a mock of a real keyStore and it is not intended to be used in a real environment.

@Component
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Value("${redis.host:localhost}")
    private String host;

    @Value("${redis.port:6379}")
    private int port;

    @Bean
    public PsiCacheProvider createCache() {
        log.info("Connecting to redis at host = {}, post = {}", host, port);
        try {
            return new RedisPsiCacheProvider(host, port);
        } catch (JedisConnectionException e){
            log.info("Redis is not reachable, continuing without cache");
            return null;
        }
    }
}
