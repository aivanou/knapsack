package com.ooyala.challenge.core;

import com.ooyala.challenge.data.Output;
import com.ooyala.challenge.data.ValidationError;

/**
 */
public interface ManagerCallback {

    void success(Output out);

    void error(ValidationError error);
}
