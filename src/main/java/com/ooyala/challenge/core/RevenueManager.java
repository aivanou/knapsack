package com.ooyala.challenge.core;

import com.ooyala.challenge.data.Input;
import com.ooyala.challenge.data.Output;

/**
 */
public interface RevenueManager {

    Output compute(Input data) throws ValidationException;

    void computeAsync(Input data, ManagerCallback callback);

    void start(int nWorkers);

    void stop();
}
