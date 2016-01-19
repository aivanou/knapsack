package com.ooyala.challenge.rest;

import com.ooyala.challenge.core.RevenueManager;
import io.dropwizard.lifecycle.Managed;

/**
 */
public class RevenueManagerService implements Managed {
    private RevenueManager manager;
    private int nTasks;

    public RevenueManagerService(RevenueManager manager, int nTasks) {
        this.manager = manager;
        this.nTasks = nTasks;
    }

    @Override public void start() throws Exception {
        manager.start(nTasks);
    }

    @Override public void stop() throws Exception {
        manager.stop();
    }
}
