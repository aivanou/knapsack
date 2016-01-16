package com.ooyala.challenge.core;

import com.ooyala.challenge.DummyInputDataSets;
import com.ooyala.challenge.core.Processor;
import com.ooyala.challenge.core.impl.ParallelRevenueProcessor;
import com.ooyala.challenge.core.impl.SequentialRevenueProcessor;
import com.ooyala.challenge.data.Input;
import com.ooyala.challenge.data.Output;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 */
public class RevenueProcessorImplTest {

    private Processor processor;

    public RevenueProcessorImplTest() {
        this.processor = new SequentialRevenueProcessor();
    }

    @Test
    public void testCompute1() {
        Input set1 = DummyInputDataSets.dataSet1();
        Output output = processor.compute(set1);
        Assert.assertEquals(output.getOutputMetadata().getTotalImpressions(), 32000000);
        Assert.assertEquals(output.getOutputMetadata().getTotalRevenue(), 3620);
    }

    @Test
    public void testCompute2() {
        Input set2 = DummyInputDataSets.dataSet2();
        Output output = processor.compute(set2);
        Assert.assertEquals(output.getOutputMetadata().getTotalImpressions(), 2000000000);
        Assert.assertEquals(output.getOutputMetadata().getTotalRevenue(), 13330000);
    }

    @Test
    public void testCompute3() {
        Input set3 = DummyInputDataSets.dataSet3();
        Output output = processor.compute(set3);
        Assert.assertEquals(output.getOutputMetadata().getTotalImpressions(), 50000000);
        Assert.assertEquals(output.getOutputMetadata().getTotalRevenue(), 51014000);
    }

    @Test
    public void testParallel1() {
        Executor exec = Executors.newCachedThreadPool();
        int tasks = 4;
        ParallelRevenueProcessor p = new ParallelRevenueProcessor(exec, tasks);
        Input set4 = DummyInputDataSets.generateRandom(300, 30000, 30, 30);
        long start = System.nanoTime();
        Output out = p.compute(set4);
        System.out.println((System.nanoTime() - start) / 1000000000.0);
        start = System.nanoTime();
        Output out1 = processor.compute(set4);
        System.out.println((System.nanoTime() - start) / 1000000000.0);
        System.out.println(out1.getOutputMetadata());
//        Assert.assertEquals(out1.getOutputMetadata(), out.getOutputMetadata());
    }

}