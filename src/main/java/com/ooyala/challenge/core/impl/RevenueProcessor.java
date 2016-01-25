package com.ooyala.challenge.core.impl;

import com.ooyala.challenge.core.Processor;
import com.ooyala.challenge.data.*;

import java.util.*;

/**
 */
public abstract class RevenueProcessor implements Processor {

    @Override
    public Output compute(Input input) {
        List<Company> removedCompanies = removeCompaniesWithZeroRevenue(input);
        List<Company> removedByDomination = removeDominatedCompanies(input.getCompanies());
        int factor = normalise(input);
        if (input.getCompanies().isEmpty()) {
            Output output = new Output(new ArrayList<>(), new OutputMetadata(0, 0));
            removedCompanies.stream().forEach(company -> output.getOutputItem().add(new OutputItem(company.getName(), 0, 0, 0)));
            return output;
        }
        Output output = compute(input.getCompanies(), input.getAvailableImpressions());
        denormalise(output, factor, input);
        removedCompanies.stream().forEach(company -> {
            output.getOutputItem().add(new OutputItem(company.getName(), 0, 0, 0));
            input.getCompanies().add(company);
        });
        removedByDomination.stream().forEach(company -> {
            output.getOutputItem().add(new OutputItem(company.getName(), 0, 0, 0));
            input.getCompanies().add(company);
        });
        return output;
    }

    protected abstract Output compute(List<Company> companies, int availableImpressions);

    private List<Company> removeDominatedCompanies(List<Company> companies) {
        companies.sort((o1, o2) -> {
            if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            }
            return Integer.compare(o1.getNumberOfImpression(), o2.getNumberOfImpression());
        });
        List<Company> companiesToRemove = new ArrayList<>();
        for (int i = 1; i < companies.size(); ++i) {
            Company toCheck = companies.get(i);
            for (int j = 0; j < i; ++j) {
                Company company = companies.get(j);
                int amount = toCheck.getNumberOfImpression() / company.getNumberOfImpression();
                if (amount * company.getRevenue() > toCheck.getRevenue()) {
                    companiesToRemove.add(toCheck);
                    break;
                }
            }
        }
        companiesToRemove.forEach(company -> companies.remove(company));
        return companiesToRemove;
    }

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

    private void denormalise(Output output, int factor, Input input) {
        for (int i = 0; i < output.getOutputItem().size(); i++) {
            OutputItem outItem = output.getOutputItem().get(i);
            Company company = input.getCompanies().get(i);
            outItem.setTotalImpression(outItem.getTotalImpression() * factor);
            company.setNumberOfImpression(company.getNumberOfImpression() * factor);
        }
        output.getOutputMetadata().setTotalImpressions(output.getOutputMetadata().getTotalImpressions() * factor);
        input.setAvailableImpressions(input.getAvailableImpressions() * factor);
    }

    private static int gcd(int n1, int n2) {
        if (n1 == 0 && n2 == 0)
            return 1;
        else if (n2 == 0)
            return n1;
        return gcd(n2, n1 % n2);
    }
}
