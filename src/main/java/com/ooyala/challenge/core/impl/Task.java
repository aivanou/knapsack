package com.ooyala.challenge.core.impl;

import com.ooyala.challenge.core.ManagerCallback;
import com.ooyala.challenge.data.Input;

/**
 */
public class Task {
    private final Input input;
    private final ManagerCallback callback;
    private final long timeAdded;

    public Task(Input input, ManagerCallback callback, long timeAdded) {
        this.input = input;
        this.callback = callback;
        this.timeAdded = timeAdded;
    }

    public Input getInput() {
        return input;
    }

    public ManagerCallback getCallback() {
        return callback;
    }

    public long getTimeAdded() {
        return timeAdded;
    }
}
