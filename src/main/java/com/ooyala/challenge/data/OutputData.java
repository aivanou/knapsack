package com.ooyala.challenge.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 */
public class OutputData {

    private String companyName;
    private long numberOfCampains;
    private long totalImpression;
    private long totalRevenue;

    public OutputData(String companyName, long numberOfCampains, long totalImpression, long totalRevenue) {
        this.companyName = companyName;
        this.numberOfCampains = numberOfCampains;
        this.totalImpression = totalImpression;
        this.totalRevenue = totalRevenue;
    }

    public OutputData() {
    }

    public void incCampains() {
        numberOfCampains += 1;
    }

    @JsonProperty
    public String getCompanyName() {
        return companyName;
    }

    @JsonProperty
    public long getNumberOfCampains() {
        return numberOfCampains;
    }

    @JsonProperty
    public long getTotalImpression() {
        return totalImpression;
    }

    @JsonProperty
    public long getTotalRevenue() {
        return totalRevenue;
    }

    @JsonProperty
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @JsonProperty
    public void setNumberOfCampains(long numberOfCampains) {
        this.numberOfCampains = numberOfCampains;
    }

    @JsonProperty
    public void setTotalImpression(long totalImpression) {
        this.totalImpression = totalImpression;
    }

    @JsonProperty
    public void setTotalRevenue(long totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        OutputData that = (OutputData) o;

        if (numberOfCampains != that.numberOfCampains)
            return false;
        if (totalImpression != that.totalImpression)
            return false;
        if (totalRevenue != that.totalRevenue)
            return false;
        return !(companyName != null ? !companyName.equals(that.companyName) : that.companyName != null);

    }

    @Override public int hashCode() {
        int result = companyName != null ? companyName.hashCode() : 0;
        result = 31 * result + (int) (numberOfCampains ^ (numberOfCampains >>> 32));
        result = 31 * result + (int) (totalImpression ^ (totalImpression >>> 32));
        result = 31 * result + (int) (totalRevenue ^ (totalRevenue >>> 32));
        return result;
    }

    @Override public String toString() {
        return "OutputData{" +
            "companyName='" + companyName + '\'' +
            ", numberOfCampains=" + numberOfCampains +
            ", totalImpression=" + totalImpression +
            ", totalRevenue=" + totalRevenue +
            '}';
    }
}
