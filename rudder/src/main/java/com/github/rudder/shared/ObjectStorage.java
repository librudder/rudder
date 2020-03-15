package com.github.rudder.shared;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple class to store objects, that are "transferred" between container and host,
 * so we can use them if someone want to call a method on them
 */
public class ObjectStorage {

    private final Map<String, Object> objects = new ConcurrentHashMap<>();

    /**
     * Get object by id
     * @param key id
     * @return object
     */
    public Object get(final String key) {
        return objects.get(key);
    }

    /**
     * Put object to storage and get generated ID
     * @param value object to store
     * @return generated object id
     */
    public String put(final Object value) {
        final UUID uuid = UUID.randomUUID();
        final String key = uuid.toString();
        objects.put(key, value);
        return key;
    }

    /**
     * Keys of object storage
     * @return keys
     */
    public Set<String> keySet() {
        return objects.keySet();
    }

}
