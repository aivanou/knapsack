package org.dynamic.core;

import org.dynamic.DummyInputDataSets;
import org.dynamic.cache.CacheManager;
import org.dynamic.cache.CacheException;
import org.dynamic.core.impl.RevenueManagerImpl;
import org.dynamic.data.Input;
import org.dynamic.data.Output;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 */
public class TestRevenueManager {

    CacheManager<Input, Output> cacheMock = mock(CacheManager.class);
    Processor processorMock = mock(Processor.class);
    ServiceManager execMock = mock(ServiceManager.class);
    RevenueManager revenueManager = new RevenueManagerImpl(cacheMock, processorMock, execMock, 1);

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
