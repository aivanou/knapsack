package org.dynamic.cache;

import org.dynamic.DummyInputDataSets;
import org.dynamic.cache.impl.InternalCacheManager;
import org.dynamic.data.Input;
import org.dynamic.data.Output;
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
