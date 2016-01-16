package com.ooyala.challenge.cache;

import java.util.concurrent.Future;

/**
 */
public interface CacheManager<Key, Value> {

    void insert(Key key, Value value);

    void insertAsync(Key key, Value value, CacheInsertCallback callback);

    Value retrieve(Key key) throws CacheException;

    Future<Value> retrieveAsync(Key key);

    boolean contains(Key key);
}
