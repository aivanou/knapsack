package com.ooyala.challenge.core;

import com.ooyala.challenge.data.Input;
import com.ooyala.challenge.data.Output;

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
