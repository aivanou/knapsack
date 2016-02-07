package org.dynamic.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Combined Internal and DTO output object
 * see @link(Input) class
 */
public class Output {

    private List<OutputItem> outputItem;
    private OutputMetadata outputMetadata;

    public Output() {
        this.outputItem = new ArrayList<>();
        this.outputMetadata = new OutputMetadata(0, 0);
    }

    public Output(List<OutputItem> outputItem, OutputMetadata outputMetadata) {
        this.outputItem = outputItem;
        this.outputMetadata = outputMetadata;
    }

    @JsonProperty
    public void setOutputItem(List<OutputItem> outputItem) {
        this.outputItem = outputItem;
    }

    @JsonProperty
    public void setOutputMetadata(OutputMetadata outputMetadata) {
        this.outputMetadata = outputMetadata;
    }

    @JsonProperty
    public List<OutputItem> getOutputItem() {
        return outputItem;
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

        if (outputItem != null ? !outputItem.equals(output.outputItem) : output.outputItem != null)
            return false;
        return !(outputMetadata != null ? !outputMetadata.equals(output.outputMetadata) : output.outputMetadata != null);

    }

    @Override public int hashCode() {
        int result = (outputMetadata != null ? outputMetadata.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        StringBuilder bldr = new StringBuilder();
        for (OutputItem out : outputItem) {
            bldr.append(out.toString() + "\n");
        }
        bldr.append(outputMetadata);
        return bldr.toString();
    }
}
