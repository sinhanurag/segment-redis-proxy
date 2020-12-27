package com.segment.cache.dao;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import org.slf4j.Logger;

public class RedisDao {
    private static Jedis client;
    private static Logger logger;

    RedisDao(String host, int port) {
        client = new Jedis(host, port,1800);
        logger = LoggerFactory.getLogger(RedisDao.class);
    }

    public String getCacheNodeFromRedis(String key) {
        try {
            String value = client.get(key);
            return value;
        } catch (Exception ex) {
            logger.error(ExceptionUtils.getStackTrace(ex));
            throw ex;
        }
    }
}
