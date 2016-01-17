package com.ooyala.challenge.core.impl;

import com.ooyala.challenge.cache.CacheException;
import com.ooyala.challenge.cache.CacheManager;
import com.ooyala.challenge.core.ManagerCallback;
import com.ooyala.challenge.core.RevenueManager;
import com.ooyala.challenge.core.Processor;
import com.ooyala.challenge.core.ValidationException;
import com.ooyala.challenge.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * The controller that is available for usage by external classes and frameworks
 */
public class RevenueManagerImpl implements RevenueManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevenueManagerImpl.class);

    private CacheManager<Input, Output> cacheManager;
    private Processor processor;

    public RevenueManagerImpl(CacheManager<Input, Output> cacheManager, Processor processor) {
        this.cacheManager = cacheManager;
        this.processor = processor;
    }

    @Override
    public Output compute(Input input) throws CacheException, ValidationException {
        ValidationError errors = validate(input);
        if (!errors.getMessages().isEmpty()) {
            throw new ValidationException(errors.toString());
        }
        return internalCompute(input);
    }

    /**
     * Technically, the internalCompute method should be executed inside a separate thread,
     * But in this case we should keep track on the amount of concurrent tasks
     * and block, delay or forbidden the execution of new requests if all workers are occupied.
     * <p>
     * This will guarantee that the server will execute the amount of tasks
     * that it actually can compute
     *
     * @param data
     * @param callback
     */
    @Override public void computeAsync(Input data, ManagerCallback callback) {
        ValidationError errors = validate(data);
        if (!errors.getMessages().isEmpty()) {
            callback.error(errors);
            return;
        }
        Output output;
        try {
            output = internalCompute(data);
        } catch (CacheException e) {
            LOGGER.error(e.getLocalizedMessage());
            callback.error(errors);
            return;
        }
        callback.success(output);
    }

    private Output internalCompute(Input input) throws CacheException {
        Output output = cacheManager.retrieve(input);
        if (output == null) {
            output = processor.compute(input);
            cacheManager.insert(input, output);
            return output;
        }
        return output;
    }

    private ValidationError validate(Input input) {
        List<String> errors = new ArrayList<>();
        if (input.getAvailableImpressions() <= 0) {
            errors.add("Available impressions should be positive integer");
        }
        for (Company company : input.getCompanies()) {
            if (company.getNumberOfImpression() < 0) {
                errors.add(String.format("Company: %s contains negative impression", company.getName()));
            }
            if (company.getRevenue() < 0) {
                errors.add(String.format("Company: %s contains negative revenue", company.getName()));
            }
        }
        return new ValidationError(errors);
    }
}
