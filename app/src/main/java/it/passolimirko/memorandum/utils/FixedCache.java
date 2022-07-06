package it.passolimirko.memorandum.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class FixedCache<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;

    public FixedCache(int size) {
        super(size + 2, 1F); //+2 to have place for the newly added element AND not fill up to cause resize
        this.maxSize = size;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
}