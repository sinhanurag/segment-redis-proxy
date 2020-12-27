package com.segment.cache.models;

public class CacheNode {
    private String key;
    private String value;
    private String expiry;

    public CacheNode(String key, String value, String expiry) {
        this.key = key;
        this.value = value;
        this.expiry = expiry;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
