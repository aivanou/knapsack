package com.ooyala.challenge.core;

import com.ooyala.challenge.cache.CacheException;
import com.ooyala.challenge.data.Input;
import com.ooyala.challenge.data.Output;

import javax.ws.rs.container.AsyncResponse;

/**
 */
public interface RevenueManager {

    Output compute(Input data) throws ValidationException;

    void computeAsync(Input data, ManagerCallback callback);
}
