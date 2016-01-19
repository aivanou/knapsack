package com.ooyala.challenge.rest;

import com.ooyala.challenge.core.ManagerCallback;
import com.ooyala.challenge.core.RevenueManager;
import com.ooyala.challenge.core.ValidationException;
import com.ooyala.challenge.data.*;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class MaxRevenueResource {
    private final RevenueManager revenueManager;

    public MaxRevenueResource(RevenueManager revenueManager) {
        this.revenueManager = revenueManager;
    }

    @POST
    @Path("/asyncCompute")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void receiveDate(Input data, @Suspended AsyncResponse asyncResponse) {
        revenueManager.computeAsync(
            data, new ManagerCallback() {
                @Override public void success(Output out) {
                    asyncResponse.resume(out);
                }

                @Override public void error(ValidationError error) {
                    asyncResponse.resume(new WebApplicationException(error.toString(), Response.Status.BAD_REQUEST));
                }
            });
    }

    @POST
    @Path("/compute")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Output receiveDate(Input data) {
        try {
            return revenueManager.compute(data);
        } catch (ValidationException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }
}
