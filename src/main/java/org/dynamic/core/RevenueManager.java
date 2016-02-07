package org.dynamic.core;

import org.dynamic.data.Input;
import org.dynamic.data.Output;

/**
 */
public interface RevenueManager {

    Output compute(Input data) throws ValidationException;

    void computeAsync(Input data, ManagerCallback callback);

    void start(int nWorkers);

    void stop();
}
