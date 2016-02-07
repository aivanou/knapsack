package org.dynamic.rest;

import org.dynamic.cache.CacheManager;
import org.dynamic.cache.impl.InternalCacheManager;
import org.dynamic.core.RevenueManager;
import org.dynamic.core.Processor;
import org.dynamic.core.ServiceManager;
import org.dynamic.core.impl.RevenueManagerImpl;
import org.dynamic.core.impl.SequentialRevenueProcessor;
import org.dynamic.core.impl.ServiceManagerImpl;
import org.dynamic.data.Input;
import org.dynamic.data.Output;
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
        int nWorkers = 4;
        int maxQueueSize = 1000;
        ExecutorService exec = Executors.newFixedThreadPool(nWorkers * 2);
        ServiceManager serviceManager = new ServiceManagerImpl(exec);
        RevenueManager revenueManager = new RevenueManagerImpl(cacheManager, processor, serviceManager, maxQueueSize);
        environment.lifecycle().manage(new RevenueManagerService(revenueManager, nWorkers));
        final MaxRevenueResource resource = new MaxRevenueResource(revenueManager);
        environment.jersey().register(resource);
    }
}
