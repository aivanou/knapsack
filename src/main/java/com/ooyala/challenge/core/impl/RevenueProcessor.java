package com.ooyala.challenge.core.impl;

import com.ooyala.challenge.core.Processor;
import com.ooyala.challenge.data.Company;
import com.ooyala.challenge.data.Input;
import com.ooyala.challenge.data.Output;
import com.ooyala.challenge.data.OutputData;

import java.util.ArrayList;
import java.util.List;

/**
 */
public abstract class RevenueProcessor implements Processor {

    @Override
    public Output compute(Input input) {
        List<Company> removedCompanies = removeCompaniesWithZeroRevenue(input);
        int factor = normalise(input);
        Output output = compute(input.getCompanies(), input.getAvailableImpressions());
        denormalise(output, factor);
        removedCompanies.stream().forEach(company -> output.getOutputData().add(new OutputData(company.getName(), 0, 0, 0)));
        return output;
    }

    protected abstract Output compute(List<Company> companies, int availableImpressions);

    private List<Company> removeCompaniesWithZeroRevenue(Input input) {
        List<Company> removedCompanies = new ArrayList<>();
        input.getCompanies().stream().filter(company -> company.getRevenue() == 0).forEach(
            company -> {
                input.getCompanies().remove(company);
                removedCompanies.add(company);
            });
        return removedCompanies;
    }

    private int normalise(Input input) {
        int factor = input.getCompanies().stream().map(Company::getNumberOfImpression).reduce(input.getAvailableImpressions(), (v1, v2) -> gcd(v1, v2));
        for (Company company : input.getCompanies()) {
            company.setNumberOfImpression(company.getNumberOfImpression() / factor);
        }
        input.setAvailableImpressions(input.getAvailableImpressions() / factor);
        return factor;
    }

    private void denormalise(Output output, int factor) {
        for (OutputData out : output.getOutputData()) {
            out.setTotalImpression(out.getTotalImpression() * factor);
        }
        output.getOutputMetadata().setTotalImpressions(output.getOutputMetadata().getTotalImpressions() * factor);
    }

    private static int gcd(int n1, int n2) {
        if (n2 == 0)
            return n1;
        return gcd(n2, n1 % n2);
    }
}
