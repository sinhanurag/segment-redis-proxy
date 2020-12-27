package com.segment.cache.dao;

import com.segment.cache.models.CacheNode;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LocalCache {
    private static Map<String, CacheNode> localCache;
    private static Queue<CacheNode> tracker;
    private static int MAX_CACHE_SIZE;
    private static  int GLOBAL_EXPIRY_DURATION;
    private static RedisDao redis;

    public LocalCache(int size, int expiry, String redisHost, int redisPort) {
        MAX_CACHE_SIZE = size;
        GLOBAL_EXPIRY_DURATION = expiry;
        localCache = new ConcurrentHashMap<>(size);
        tracker = new ConcurrentLinkedQueue<>();
        redis = new RedisDao(redisHost, redisPort);
    }

    public String get(String key) {
        if (localCache.get(key) != null) {
            CacheNode node = localCache.get(key);
            LocalDateTime expiryTime = LocalDateTime.parse(node.getExpiry());
            if (LocalDateTime.now().isAfter(expiryTime)) {
                tracker.remove(node);
                localCache.remove(key);
                return null;
            }
            tracker.remove(node);
            tracker.add(node);
            return node.getValue();
        } else {
            String value = redis.getCacheNodeFromRedis(key);
            if (value != null && value.length() > 0) {
                LocalDateTime currentDateTime = LocalDateTime.now();
                LocalDateTime expiry = currentDateTime.plus(GLOBAL_EXPIRY_DURATION, ChronoUnit.SECONDS);
                CacheNode node = new CacheNode(key, value, expiry.toString());
                evictIfRequired();
                localCache.put(key,node);
                tracker.add(node);
            }
            return value;
        }
    }
    private void evictIfRequired() {
        if(localCache.size() < MAX_CACHE_SIZE) {
            return;
        }
        CacheNode nodeToBeEvicted = tracker.poll();
        localCache.remove(nodeToBeEvicted.getKey());
    }
}
