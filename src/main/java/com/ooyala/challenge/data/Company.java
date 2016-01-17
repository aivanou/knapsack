package com.ooyala.challenge.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Combined internal and DTO object
 * see @link(Input) class
 */
public class Company {

    private String name;
    private int numberOfImpression;
    private int revenue;

    public Company() {
        this("", 0, 0);
    }

    public Company(String name, int numberOfImpression, int revenue) {
        this.name = name;
        this.numberOfImpression = numberOfImpression;
        this.revenue = revenue;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public int getNumberOfImpression() {
        return numberOfImpression;
    }

    @JsonProperty
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    public void setNumberOfImpression(int numberOfImpression) {
        this.numberOfImpression = numberOfImpression;
    }

    @JsonProperty
    public void setRevenue(int revenue) {
        this.revenue = revenue;
    }

    @JsonProperty
    public int getRevenue() {
        return revenue;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Company company = (Company) o;

        if (numberOfImpression != company.numberOfImpression)
            return false;
        if (revenue != company.revenue)
            return false;
        return !(name != null ? !name.equals(company.name) : company.name != null);

    }

    @Override public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (int) (numberOfImpression ^ (numberOfImpression >>> 32));
        result = 31 * result + (int) (revenue ^ (revenue >>> 32));
        return result;
    }

    @Override public String toString() {
        return "Company{" +
            "name='" + name + '\'' +
            ", numberOfImpression=" + numberOfImpression +
            ", revenue=" + revenue +
            '}';
    }
}
