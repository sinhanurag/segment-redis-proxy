package com.segment.cache.dao;

import com.segment.cache.models.CacheNode;
import redis.clients.jedis.Jedis;

import java.time.Instant;

public class RedisDao {
    private static Jedis client;

    RedisDao(String host, int port) {
        client = new Jedis(host, port,1800);
    }

    public CacheNode getCacheNodeFromRedis(String key) {
        String value = client.get(key);
        if (value != null && value.length() > 0) {
            Long expiry = Instant.now().getEpochSecond() + 60;
            CacheNode node = new CacheNode(key, value, expiry);
            return node;
        }
        return null;
    }
}
