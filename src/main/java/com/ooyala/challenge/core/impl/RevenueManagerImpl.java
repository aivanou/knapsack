package com.ooyala.challenge.core.impl;

import com.ooyala.challenge.cache.CacheException;
import com.ooyala.challenge.cache.CacheManager;
import com.ooyala.challenge.core.ManagerCallback;
import com.ooyala.challenge.core.RevenueManager;
import com.ooyala.challenge.core.Processor;
import com.ooyala.challenge.core.ValidationException;
import com.ooyala.challenge.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * The controller that is available for usage by external classes and frameworks
 * <p>
 * Note: If more than single processor should be used, introduce the factory
 */
public class RevenueManagerImpl implements RevenueManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevenueManagerImpl.class);

    private final CacheManager<Input, Output> cacheManager;
    private final Processor processor;
    private final ExecutorService exec;
    private final BlockingDeque<Task> workQueue;
    private static int MAX_WAIT_FOR_COMPLETION_SECONDS = 60;
    private static int MAX_WAIT_FOR_TERMINATION_SECONDS = 5;

    public RevenueManagerImpl(CacheManager<Input, Output> cacheManager,
        Processor processor, ExecutorService exec, int maxQueueSize) {
        this.exec = exec;
        this.cacheManager = cacheManager;
        this.processor = processor;
        this.workQueue = new LinkedBlockingDeque<>(maxQueueSize);
    }

    @Override
    public Output compute(Input input) throws ValidationException {
        ValidationError errors = validate(input);
        if (!errors.getMessages().isEmpty()) {
            throw new ValidationException(errors.toString());
        }
        return internalCompute(input);
    }

    /**
     * Technically, the internalCompute method should be executed inside a separate thread,
     * But in this case we should keep track on the amount of concurrent tasks
     * and block, delay or forbidden the execution of new requests if all workers are occupied.
     * <p>
     * This will guarantee that the server will execute the amount of tasks
     * that it actually can compute
     *
     * @param data
     * @param callback
     */
    @Override public void computeAsync(Input data, ManagerCallback callback) {
        ValidationError errors = validate(data);
        if (!errors.getMessages().isEmpty()) {
            callback.error(errors);
            return;
        }
        if (!workQueue.offer(new Task(data, callback, System.nanoTime()))) {
            callback.error(new ValidationError("Sorry. There are too many tasks right now."));
        }
    }

    @Override public void start(int nWorkers) {
        for (int i = 0; i < nWorkers; ++i) {
            exec.execute(new Worker());
        }
    }

    @Override public void stop() {
        try {
            exec.awaitTermination(MAX_WAIT_FOR_TERMINATION_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            List<Runnable> tasks = exec.shutdownNow();
            LOGGER.warn("Shutting down was not successful. Amount of tasks that stopped: " + tasks.size());
        }
    }

    private Output internalCompute(Input input) {
        Output output = null;
        try {
            output = cacheManager.retrieve(input);
        } catch (CacheException e) {
            /**If the external system is used as a cache, we should
             * signal the error to some service or use programs like Zabbix*/
            LOGGER.error(e.getLocalizedMessage());
        }
        if (output == null) {
            output = processor.compute(input);
            cacheManager.insert(input, output);
        }
        return output;
    }

    private ValidationError validate(Input input) {
        List<String> errors = new ArrayList<>();
        if (input.getAvailableImpressions() <= 0) {
            errors.add("Available impressions should be positive integer");
        }
        for (Company company : input.getCompanies()) {
            if (company.getNumberOfImpression() < 0) {
                errors.add(String.format("Company: %s contains negative impression", company.getName()));
            }
            if (company.getRevenue() < 0) {
                errors.add(String.format("Company: %s contains negative revenue", company.getName()));
            }
        }
        return new ValidationError(errors);
    }

    private class Worker implements Runnable {

        private volatile boolean interrupt = false;
        private static final int MAX_WAIT_SECONDS = 2;

        @Override public void run() {
            while (!interrupt) {
                Task task = waitForTask();
                if (interrupt) {
                    return;
                }
                if (isTimeExceeded(task.timeAdded)) {
                    task.callback.error(new ValidationError("Sorry. Service was too busy to compute"));
                    continue;
                }
                List<Callable<Output>> toExecute = Arrays.asList(() -> processor.compute(task.input));
                try {
                    Output result = exec.invokeAny(toExecute, MAX_WAIT_FOR_COMPLETION_SECONDS, TimeUnit.SECONDS);
                    task.callback.success(result);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    LOGGER.error(e.getLocalizedMessage());
                    task.callback.error(new ValidationError(e.getMessage()));
                }
            }
        }

        private boolean isTimeExceeded(long startTime) {
            return (System.nanoTime() - startTime) / 1000000000.0 > MAX_WAIT_FOR_COMPLETION_SECONDS;
        }

        private Task waitForTask() {
            Task task = null;
            try {
                while ((task = workQueue.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS)) == null) {
                    if (interrupt) {
                        return null;
                    }
                }
            } catch (InterruptedException e) {
                interrupt = true;
                return null;
            }
            return task;
        }

        public boolean isInterrupt() {
            return interrupt;
        }

        public void setInterrupt(boolean interrupt) {
            this.interrupt = interrupt;
        }
    }

    private class Task {
        private Input input;
        private ManagerCallback callback;
        private long timeAdded;

        public Task(Input input, ManagerCallback callback, long timeAdded) {
            this.input = input;
            this.callback = callback;
            this.timeAdded = timeAdded;
        }
    }
}
