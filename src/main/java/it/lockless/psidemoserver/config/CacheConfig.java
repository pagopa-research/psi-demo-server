package it.lockless.psidemoserver.config;

import it.lockless.psidemoserver.service.cache.RedisPsiCacheProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import psi.cache.PsiCacheProvider;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * The provided cache is implemented based on redis.
 * If the redis server is not reachable to the specified endpoint, the cache is not used.
 *
 * Helpful commands to run redis locally with docker:
 * - Create redis docker: docker run --name redis -p 6379:6379 -d redis
 * - After stopped, can run again with: docker start redis
 * - Connect to cli: docker exec -it redis redis-cli
 *
 */

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
