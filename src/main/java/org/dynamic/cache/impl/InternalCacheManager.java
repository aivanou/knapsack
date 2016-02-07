package org.dynamic.cache.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.dynamic.cache.CacheManager;
import org.dynamic.cache.CacheException;
import org.dynamic.cache.CacheInsertCallback;
import org.dynamic.data.Input;
import org.dynamic.data.Output;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 */
public class InternalCacheManager implements CacheManager<Input, Output> {

    private Cache<Input, Output> internalCache;

    public InternalCacheManager() {
        this.internalCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
    }

    @Override
    public void insert(Input inputData, Output output) {
        internalCache.put(inputData, output);
    }

    @Override
    public void insertAsync(Input inputData, Output output, CacheInsertCallback callback) {
        internalCache.put(inputData, output);
        callback.success();
    }

    @Override
    public Output retrieve(Input inputData) throws CacheException {
        return internalCache.getIfPresent(inputData);
    }

    @Override
    public Future<Output> retrieveAsync(final Input inputData) {
        return new Future<Output>() {
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            public boolean isCancelled() {
                return false;
            }

            public boolean isDone() {
                return true;
            }

            public Output get() throws InterruptedException, ExecutionException {
                return internalCache.getIfPresent(inputData);
            }

            public Output get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return internalCache.getIfPresent(inputData);
            }
        };
    }

    @Override public boolean contains(Input input) {
        return internalCache.getIfPresent(input) != null;
    }
}
