package com.ooyala.challenge.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Combined internal and DTO output object
 * see @link(Input) class
 */
public class OutputMetadata {

    private long totalImpressions;
    private long totalRevenue;

    public OutputMetadata(long totalImpressions, long totalRevenue) {
        this.totalImpressions = totalImpressions;
        this.totalRevenue = totalRevenue;
    }

    public OutputMetadata() {
    }

    @JsonProperty
    public long getTotalImpressions() {
        return totalImpressions;
    }

    @JsonProperty
    public void setTotalImpressions(long totalImpressions) {
        this.totalImpressions = totalImpressions;
    }

    @JsonProperty
    public void setTotalRevenue(long totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    @JsonProperty
    public long getTotalRevenue() {
        return totalRevenue;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        OutputMetadata that = (OutputMetadata) o;

        if (totalImpressions != that.totalImpressions)
            return false;
        return totalRevenue == that.totalRevenue;

    }

    @Override public int hashCode() {
        int result = (int) (totalImpressions ^ (totalImpressions >>> 32));
        result = 31 * result + (int) (totalRevenue ^ (totalRevenue >>> 32));
        return result;
    }

    @Override public String toString() {
        return "OutputMetadata{" +
            "totalImpressions=" + totalImpressions +
            ", totalRevenue=" + totalRevenue +
            '}';
    }
}
