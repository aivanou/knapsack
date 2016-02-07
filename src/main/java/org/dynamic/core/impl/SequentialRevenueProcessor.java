package org.dynamic.core.impl;

import org.dynamic.core.Processor;
import org.dynamic.data.Company;
import org.dynamic.data.Output;
import org.dynamic.data.OutputItem;
import org.dynamic.data.OutputMetadata;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Usual unbounded knapsack dynamic algorithm
 */
public class SequentialRevenueProcessor extends RevenueProcessor implements Processor {

    @Override
    protected Output compute(List<Company> companies, int availableImpressions) {
        int[] revenues = new int[availableImpressions + 1];
        int[] acceptedCompanies = new int[availableImpressions + 1];
        int totalImpressions = 0;
        int totalRevenue = 0;
        for (int currentImpressions = 1; currentImpressions <= availableImpressions; ++currentImpressions) {
            int maxCurrentRevenue = 0;
            int companyToAdd = -1;
            for (int i = 0; i < companies.size(); ++i) {
                Company company = companies.get(i);
                if (company.getNumberOfImpression() > currentImpressions) {
                    continue;
                }
                int currentRevenue = revenues[currentImpressions - company.getNumberOfImpression()] + company.getRevenue();
                if (maxCurrentRevenue < currentRevenue) {
                    companyToAdd = i;
                    maxCurrentRevenue = currentRevenue;
                }
            }
            if (maxCurrentRevenue > totalRevenue) {
                totalRevenue = maxCurrentRevenue;
                totalImpressions = currentImpressions;
            }
            revenues[currentImpressions] = maxCurrentRevenue;
            acceptedCompanies[currentImpressions] = companyToAdd;
        }
        OutputMetadata outputMetadata = new OutputMetadata(totalImpressions, totalRevenue);
        List<OutputItem> outputItem = gatherCompanies(companies, totalImpressions, acceptedCompanies);
        return new Output(outputItem, outputMetadata);
    }

    private List<OutputItem> gatherCompanies(List<Company> companies, int totalImpressions, int[] acceptedCompanies) {
        List<OutputItem> outputItems = companies.stream()
            .map(company -> new OutputItem(company.getName(), 0, 0, 0))
            .collect(Collectors.toList());
        while (totalImpressions > 0) {
            int companyToAdd = acceptedCompanies[totalImpressions];
            Company company = companies.get(companyToAdd);
            OutputItem output = outputItems.get(companyToAdd);
            increaseOutputValues(output, company);
            totalImpressions -= company.getNumberOfImpression();
        }
        return outputItems;
    }

    private void increaseOutputValues(OutputItem outputItem, Company company) {
        outputItem.incCampains();
        outputItem.increaseTotalImpressions(company.getNumberOfImpression());
        outputItem.increaseTotalRevenue(company.getRevenue());
    }
}
