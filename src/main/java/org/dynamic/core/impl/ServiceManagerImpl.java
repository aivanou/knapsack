package org.dynamic.core.impl;

import org.dynamic.core.ServiceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 */
public class ServiceManagerImpl implements ServiceManager {
    private final List<Future<?>> services;
    private final ExecutorService exec;

    public ServiceManagerImpl(ExecutorService exec) {
        this.services = new ArrayList<>();
        this.exec = exec;
    }

    @Override
    public void startService(Runnable service) {
        services.add(exec.submit(service));
    }

    @Override
    public void interruptAll() {
        for (Future<?> service : services) {
            service.cancel(true);
        }
    }

    @Override
    public void awaitTermination(long time, TimeUnit unit) throws InterruptedException {
        exec.shutdown();
        exec.awaitTermination(time, unit);
    }

    @Override
    public void terminate() {
        exec.shutdownNow();
    }

    @Override
    public <T> T computeAsync(Callable<T> task, long timeout, TimeUnit unit) throws TimeoutException {
        List<Callable<T>> tasks = Arrays.asList(task);
        try {
            return exec.invokeAny(tasks, timeout, unit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new TimeoutException(e.getMessage());
        }
    }
}
