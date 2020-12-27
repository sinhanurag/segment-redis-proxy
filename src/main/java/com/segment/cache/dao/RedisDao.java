package com.segment.cache.dao;

import redis.clients.jedis.Jedis;

public class RedisDao {
    private static Jedis client;

    RedisDao(String host, int port) {
        client = new Jedis(host, port,1800);
    }

    public String getCacheNodeFromRedis(String key) {
        String value = client.get(key);
        return value;
    }
}
