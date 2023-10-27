package edu.gatech.chai.VRDR.model;

import java.time.OffsetDateTime;

public class CacheItem<V> {
    private V value;
    private OffsetDateTime lastAccessed;

    public CacheItem(V value) {
        this.value = value;
        this.lastAccessed = OffsetDateTime.now();
    }

    public V getValue() {
        lastAccessed = OffsetDateTime.now();
        return value;
    }

    public void setValue(V value) {
        if (value == null) {
            throw new IllegalArgumentException("Do not set the value to null.");
        }
        this.value = value;
    }

    public OffsetDateTime getLastAccessed() {
        return lastAccessed;
    }
}
