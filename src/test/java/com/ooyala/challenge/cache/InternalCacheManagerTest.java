package com.ooyala.challenge.cache;

import com.ooyala.challenge.DummyInputDataSets;
import com.ooyala.challenge.cache.impl.InternalCacheManager;
import com.ooyala.challenge.data.Input;
import com.ooyala.challenge.data.Output;
import org.junit.Assert;
import org.junit.Test;

/**
 */
public class InternalCacheManagerTest {

    private CacheManager<Input, Output> cacheManager;

    public InternalCacheManagerTest() {
        this.cacheManager = new InternalCacheManager();
    }

    @Test
    public void retrieveValue() {
        try {
            Input inputData = DummyInputDataSets.dataSet1();
            Output out = DummyInputDataSets.dummyOutputFromInput(inputData);
            cacheManager.insert(inputData, out);
            Output toTest = cacheManager.retrieve(inputData);
            Assert.assertEquals(toTest, out);
        } catch (CacheException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void retrieveNonExistValue() {
        try {
            Input inputData = DummyInputDataSets.dataSet1();
            Output out = cacheManager.retrieve(inputData);
            Assert.assertNull(out);
        } catch (CacheException e) {
            Assert.fail(e.getMessage());
        }
    }
}
