package com.github.rudder.shared;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectStorage {

    private final Map<String, Object> objects = new ConcurrentHashMap<>();

    public Object get(final String key) {
        return objects.get(key);
    }

    public String put(final Object value) {
        final UUID uuid = UUID.randomUUID();
        final String key = uuid.toString();
        objects.put(key, value);
        return key;
    }

}
