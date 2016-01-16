package com.ooyala.challenge.core;


import com.ooyala.challenge.DummyInputDataSets;
import com.ooyala.challenge.cache.CacheException;
import com.ooyala.challenge.cache.CacheManager;
import com.ooyala.challenge.core.impl.RevenueManagerImpl;
import com.ooyala.challenge.data.Input;
import com.ooyala.challenge.data.Output;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 */
public class TestRevenueManager {

    CacheManager<Input, Output> cacheMock = mock(CacheManager.class);
    Processor processorMock = mock(Processor.class);
    RevenueManager revenueManager = new RevenueManagerImpl(cacheMock, processorMock);

    @Test
    public void testRevenueComputation() throws IOException {
        Input input = DummyInputDataSets.dataSet1();
        Output templateOutput = DummyInputDataSets.dummyOutputFromInput(input);
        try {
            when(cacheMock.retrieve(input)).thenReturn(null);
            when(processorMock.compute(input)).thenReturn(templateOutput);
            Output output = revenueManager.compute(input);
            Assert.assertEquals(output, templateOutput);
        } catch (CacheException | ValidationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(expected = ValidationException.class)
    public void testIncorrectInputData() throws CacheException, ValidationException {
        Input in = DummyInputDataSets.incorrectDataSet();
        revenueManager.compute(in);
    }

    @Test
    public void testCacheInvoke() throws IOException {
        Input input = DummyInputDataSets.dataSet1();
        Output templateOutput = DummyInputDataSets.dummyOutputFromInput(input);
        try {
            when(cacheMock.retrieve(input)).thenReturn(templateOutput);
            Output output = revenueManager.compute(input);
            Assert.assertEquals(output, templateOutput);
        } catch (CacheException | ValidationException e) {
            Assert.fail(e.getMessage());
        }
    }
}
