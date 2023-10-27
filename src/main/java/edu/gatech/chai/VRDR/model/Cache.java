package edu.gatech.chai.VRDR.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Cache<K, V> {
    private final ConcurrentMap<K, CacheItem<V>> cached;
    private final int minimumCacheSize;
    private final Function<K, CacheItem<V>> retriever;
    private CacheSettings settings;

    public Cache() {
        this(CacheSettings.createDefault());
    }

    public Cache(Function<K, V> retrieveFunction) {
        this(retrieveFunction, CacheSettings.createDefault());
    }

    public Cache(CacheSettings settings) {
        this(null, settings);
    }

    public Cache(Function<K, V> retrieveFunction, CacheSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("settings cannot be null");
        }
        cached = new ConcurrentHashMap<>();
        retriever = retrieveFunction != null ? key -> new CacheItem<>(retrieveFunction.apply(key)) : null;
        this.settings = settings.clone();
        minimumCacheSize = (int) Math.floor(settings.getMaxCacheSize() * 0.9);
    }

    public V getValue(K key) {
        if (retriever == null) {
            CacheItem<V> foundItem = cached.get(key);
            return foundItem != null ? foundItem.getValue() : null;
        } else {
            CacheItem<V> cachedItem = cached.computeIfAbsent(key, retriever);
            enforceMaxItems();
            return cachedItem.getValue();
        }
    }

    public V getValueOrAdd(K key, V value) {
        CacheItem<V> cachedItem = cached.computeIfAbsent(key, k -> new CacheItem<>(value));
        enforceMaxItems();
        return cachedItem.getValue();
    }

    private void enforceMaxItems() {
        int currentCount = cached.size();
        if (currentCount > settings.getMaxCacheSize()) {
            // first copy the key value pairs in an array. Otherwise we could have a race condition. See for more information:
            // https://stackoverflow.com/questions/11692389/getting-argument-exception-in-concurrent-dictionary-when-sorting-and-displaying
            Map.Entry<K, CacheItem<V>>[] copy = cached.entrySet().toArray(new Map.Entry[0]);
            List<Map.Entry<K, CacheItem<V>>> oldestItems = Arrays.stream(copy)
                    .sorted(Comparator.comparing(entry -> entry.getValue().getLastAccessed()))
                    .skip(minimumCacheSize)
                    .collect(Collectors.toList());
            oldestItems.forEach(item -> cached.remove(item.getKey()));
        }
    }

    public CacheSettings getSettings() {
        return settings;
    }

    public void setSettings(CacheSettings settings) {
        this.settings = settings;
    }

    public Function<K, CacheItem<V>> getRetriever() {
        return retriever;
    }
}

