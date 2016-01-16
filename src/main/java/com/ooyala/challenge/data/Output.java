package com.ooyala.challenge.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Combined Internal and DTO output object
 */
public class Output {

    private Collection<OutputData> outputData;
    private OutputMetadata outputMetadata;

    public Output() {
        this.outputData = new ArrayList<>();
        this.outputMetadata = new OutputMetadata(0, 0);
    }

    public Output(Collection<OutputData> outputData, OutputMetadata outputMetadata) {
        this.outputData = outputData;
        this.outputMetadata = outputMetadata;
    }

    @JsonProperty
    public void setOutputData(List<OutputData> outputData) {
        this.outputData = outputData;
    }

    @JsonProperty
    public void setOutputMetadata(OutputMetadata outputMetadata) {
        this.outputMetadata = outputMetadata;
    }

    @JsonProperty
    public Collection<OutputData> getOutputData() {
        return outputData;
    }

    @JsonProperty
    public OutputMetadata getOutputMetadata() {
        return outputMetadata;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Output output = (Output) o;

        if (outputData != null ? !outputData.equals(output.outputData) : output.outputData != null)
            return false;
        return !(outputMetadata != null ? !outputMetadata.equals(output.outputMetadata) : output.outputMetadata != null);

    }

    @Override public int hashCode() {
        int result = outputData != null ? outputData.hashCode() : 0;
        result = 31 * result + (outputMetadata != null ? outputMetadata.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        StringBuilder bldr = new StringBuilder();
        for (OutputData out : outputData) {
            bldr.append(out.toString() + "\n");
        }
        bldr.append(outputMetadata);
        return bldr.toString();
    }
}
