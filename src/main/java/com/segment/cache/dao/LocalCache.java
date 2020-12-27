package com.segment.cache.dao;

import com.segment.cache.models.CacheNode;

import java.time.Instant;
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
            if (node.getExpiry()> Instant.now().getEpochSecond()) {
                tracker.remove(node);
                localCache.remove(key);
                return null;
            }
            tracker.remove(node);
            tracker.add(node);
            return node.getValue();
        } else {
            CacheNode node = redis.getCacheNodeFromRedis(key);
            if (node != null) {
                evictIfRequired();
                localCache.put(key,node);
                tracker.add(node);
                return node.getValue();
            }
           return null;
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
