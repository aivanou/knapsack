package com.ooyala.challenge.core.impl;

import com.ooyala.challenge.core.Processor;
import com.ooyala.challenge.data.*;

import java.util.ArrayList;
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
        List<OutputData> outputData = gatherCompanies(companies, totalImpressions, acceptedCompanies);
        return new Output(outputData, outputMetadata);
    }

    private List<OutputData> gatherCompanies(List<Company> companies, int totalImpressions, int[] acceptedCompanies) {
        List<OutputData> outputData = new ArrayList<>(companies.size());
        outputData.addAll(companies.stream()
            .map(company -> new OutputData(company.getName(), 0, company.getNumberOfImpression(), company.getRevenue()))
            .collect(Collectors.toList()));
        while (totalImpressions > 0) {
            int companyToAdd = acceptedCompanies[totalImpressions];
            outputData.get(companyToAdd).incCampains();
            totalImpressions -= companies.get(companyToAdd).getNumberOfImpression();
        }
        outputData.stream().forEach(out -> {
            out.setTotalImpression(out.getTotalImpression() * out.getNumberOfCampains());
            out.setTotalRevenue(out.getTotalRevenue() * out.getNumberOfCampains());
        });
        return outputData;
    }
}
