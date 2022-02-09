package it.lockless.psidemoserver.service.cache;

import it.lockless.psidemoserver.util.exception.CacheKeyAlreadyWrittenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import psi.cache.PsiCacheProvider;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Optional;

/**
 * Implementation of a PsiCacheProvided based on redis.
 * */

public class RedisPsiCacheProvider implements PsiCacheProvider {

    private static final Logger log = LoggerFactory.getLogger(RedisPsiCacheProvider.class);

    private final JedisPool jedisPool;

    public RedisPsiCacheProvider(String host, int port) {
        this.jedisPool = new JedisPool(host, port);
        Jedis jedis = jedisPool.getResource();
        jedis.ping();
        this.jedisPool.returnResource(jedis);
    }

    /**
     * Retrieve the value linked to a given key.
     *
     * @param key   key corresponding to the value to be retrieved
     *
     * @return an Optional containing the the cached value if present, Optional.empty() otherwise
     */
    @Override
    public Optional<String> get(String key) {
        log.trace("Calling get with key = {}", key);
        Jedis jedis = this.jedisPool.getResource();
        String cachedResponse = jedis.get(key);
        this.jedisPool.returnResource(jedis);
        if(cachedResponse == null)
            return Optional.empty();
        else return Optional.of(cachedResponse);
    }

    /**
     * Stores the pair <key, value> into the cache. If the key exists, it is not replaced
     *
     * @param key       key corresponding to the value to be stored.
     * @param value     value to be stored.
     */

    @Override
    public void put(String key, String value) {
        log.trace("Calling put with key = {}, value = {}", key, value);
        Jedis jedis = this.jedisPool.getResource();
        long response = jedis.setnx(key, value);
        this.jedisPool.returnResource(jedis);
        if (response == 0)
            throw new CacheKeyAlreadyWrittenException();
    }

}
