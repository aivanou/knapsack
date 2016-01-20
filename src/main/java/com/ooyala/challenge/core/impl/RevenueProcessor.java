package com.ooyala.challenge.core.impl;

import com.ooyala.challenge.core.Processor;
import com.ooyala.challenge.data.*;
import com.sun.corba.se.impl.orbutil.ObjectUtility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 */
public abstract class RevenueProcessor implements Processor {

    @Override
    public Output compute(Input input) {
        List<Company> removedCompanies = removeCompaniesWithZeroRevenue(input);
        int factor = normalise(input);
        if (input.getCompanies().isEmpty()) {
            Output output = new Output(new ArrayList<>(), new OutputMetadata(0, 0));
            removedCompanies.stream().forEach(company -> output.getOutputItem().add(new OutputItem(company.getName(), 0, 0, 0)));
            return output;
        }
        Output output = compute(input.getCompanies(), input.getAvailableImpressions());
        denormalise(output, factor);
        removedCompanies.stream().forEach(company -> output.getOutputItem().add(new OutputItem(company.getName(), 0, 0, 0)));
        return output;
    }

    protected abstract Output compute(List<Company> companies, int availableImpressions);

    private List<Company> removeCompaniesWithZeroRevenue(Input input) {
        List<Company> removedCompanies = new ArrayList<>();
        for (Iterator<Company> it = input.getCompanies().iterator(); it.hasNext(); ) {
            Company company = it.next();
            if (company.getRevenue() == 0) {
                removedCompanies.add(company);
                it.remove();
            }
        }
        return removedCompanies;
    }

    private int normalise(Input input) {
        int factor = input.getCompanies().stream().map(Company::getNumberOfImpression).reduce(input.getAvailableImpressions(), (v1, v2) -> gcd(v1, v2));
        input.getCompanies().forEach(company -> company.setNumberOfImpression(company.getNumberOfImpression() / factor));
        input.setAvailableImpressions(input.getAvailableImpressions() / factor);
        return factor;
    }

    private void denormalise(Output output, int factor) {
        output.getOutputItem().stream().forEach(out -> out.setTotalImpression(out.getTotalImpression() * factor));
        output.getOutputMetadata().setTotalImpressions(output.getOutputMetadata().getTotalImpressions() * factor);
    }

    private static int gcd(int n1, int n2) {
        if (n2 == 0)
            return n1;
        return gcd(n2, n1 % n2);
    }
}
