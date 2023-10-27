package edu.gatech.chai.VRDR.model;

public class CacheSettings {
    private int maxCacheSize = 500;

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    public static CacheSettings createDefault() {
        return new CacheSettings();
    }

    public CacheSettings() {}

    public CacheSettings(CacheSettings other) {
        if (other == null) {
            throw new IllegalArgumentException("other cannot be null");
        }
        this.maxCacheSize = other.maxCacheSize;
    }

    public CacheSettings clone() {
        return new CacheSettings(this);
    }
}

