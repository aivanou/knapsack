package com.ooyala.challenge.core.impl;

import com.ooyala.challenge.core.Processor;

import java.util.concurrent.BlockingQueue;

/**
 */
public class ProcessorService implements Runnable {
    private final Processor processor;
    private final BlockingQueue<Task> workQueue;

    public ProcessorService(Processor processor, BlockingQueue<Task> workQueue) {
        this.processor = processor;
        this.workQueue = workQueue;
    }

    @Override
    public void run() {

    }
}
