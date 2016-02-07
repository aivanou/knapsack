package org.dynamic.core;

import org.dynamic.data.Input;
import org.dynamic.data.Output;

/**
 */
public interface Processor {

    /***
     * Computes the maximum amount of impressions
     *
     * @param inputData
     * @return the optimal solutions, throws Exception if the input has incorrect format
     */
    Output compute(Input inputData);
}
