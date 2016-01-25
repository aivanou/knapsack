package com.ooyala.challenge.core;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 */
public interface ServiceManager {

    void startService(Runnable service);

    void interruptAll();

    void awaitTermination(long time, TimeUnit unit) throws InterruptedException;

    void terminate();

    <T> T computeAsync(Callable<T> task, long timeout, TimeUnit unit) throws TimeoutException;
}
