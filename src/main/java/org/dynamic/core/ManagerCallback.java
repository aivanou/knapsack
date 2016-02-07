package org.dynamic.core;

import org.dynamic.data.Output;
import org.dynamic.data.ValidationError;

/**
 */
public interface ManagerCallback {

    void success(Output out);

    void error(ValidationError error);
}
