package com.ooyala.challenge.rest;

import com.ooyala.challenge.cache.CacheManager;
import com.ooyala.challenge.cache.impl.InternalCacheManager;
import com.ooyala.challenge.core.RevenueManager;
import com.ooyala.challenge.core.Processor;
import com.ooyala.challenge.core.impl.RevenueManagerImpl;
import com.ooyala.challenge.core.impl.SequentialRevenueProcessor;
import com.ooyala.challenge.data.Input;
import com.ooyala.challenge.data.Output;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 */
public class MaxRevenueApplication extends Application<MaxRevenueConfiguration> {
    public static void main(String[] args) throws Exception {
        new MaxRevenueApplication().run(args);
    }

    @Override
    public String getName() {
        return "Max-Revenue";
    }

    @Override
    public void initialize(Bootstrap<MaxRevenueConfiguration> bootstrap) {
    }

    @Override
    public void run(MaxRevenueConfiguration configuration, Environment environment) {
        Processor processor = new SequentialRevenueProcessor();
        CacheManager<Input, Output> cacheManager = new InternalCacheManager();
        ExecutorService exec = Executors.newCachedThreadPool();
        RevenueManager revenueManager = new RevenueManagerImpl(cacheManager, processor, exec);
        revenueManager.start(4);
        final MaxRevenueResource resource = new MaxRevenueResource(revenueManager);
        environment.jersey().register(resource);
    }
}
