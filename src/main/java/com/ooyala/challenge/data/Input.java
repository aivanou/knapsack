package com.ooyala.challenge.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Combined Internal and DTO input object
 */
public class Input {

    private int availableImpressions;
    private List<Company> companies;

    public Input() {
        this.companies = new ArrayList<>();
        this.availableImpressions = 0;
    }

    public Input(int availableImpressions, List<Company> companies) {
        this.availableImpressions = availableImpressions;
        this.companies = companies;
    }

    @JsonProperty
    public int getAvailableImpressions() {
        return availableImpressions;
    }

    @JsonProperty
    public void setAvailableImpressions(int availableImpressions) {
        this.availableImpressions = availableImpressions;
    }

    @JsonProperty
    public void setCompanies(List<Company> companies) {
        this.companies = companies;
    }

    @JsonProperty
    public List<Company> getCompanies() {
        return companies;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Input inputData = (Input) o;

        if (availableImpressions != inputData.availableImpressions)
            return false;
        return !(companies != null ? !companies.equals(inputData.companies) : inputData.companies != null);

    }

    @Override public int hashCode() {
        int result = (int) (availableImpressions ^ (availableImpressions >>> 32));
        result = 31 * result + (companies != null ? companies.hashCode() : 0);
        return result;
    }
}
