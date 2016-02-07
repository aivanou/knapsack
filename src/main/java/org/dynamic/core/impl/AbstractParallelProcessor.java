package org.dynamic.core.impl;

import org.dynamic.data.Company;
import org.dynamic.data.Output;
import org.dynamic.data.OutputItem;
import org.dynamic.data.OutputMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 */
public abstract class AbstractParallelProcessor extends RevenueProcessor {

    protected Output translate(Result result, List<Company> companies) {
        List<OutputItem> items = gatherCompanies(result, companies);
        return new Output(items, new OutputMetadata(result.totalCapacity, result.maxRevenue));
    }

    protected List<OutputItem> gatherCompanies(Result result, List<Company> companies) {
        List<OutputItem> items = companies.stream()
            .map(company -> new OutputItem(company.getName(), 0, 0, 0))
            .collect(Collectors.toList());
        int curr = result.totalCapacity;
        while (curr > 0) {
            int companyIndex = result.companies[curr];
            Company company = companies.get(companyIndex);
            OutputItem item = items.get(companyIndex);
            increaseOutputValues(item, company);
            curr -= company.getNumberOfImpression();
        }
        return items;
    }

    protected void increaseOutputValues(OutputItem outputItem, Company company) {
        outputItem.incCampains();
        outputItem.increaseTotalImpressions(company.getNumberOfImpression());
        outputItem.increaseTotalRevenue(company.getRevenue());
    }

    protected Result internalCompute(List<Company> companies, int availableImpressions, int from, int to) {
        int[] revenues = new int[availableImpressions + 1];
        int maxCapacity = 0;
        int maxRevenue = 0;
        int[] acceptedCompanies = new int[availableImpressions + 1];
        for (int capacity = 1; capacity <= availableImpressions; ++capacity) {
            int maxCurrentRevenue = 0;
            acceptedCompanies[capacity] = -1;
            for (int i = from; i < to; ++i) {
                Company company = companies.get(i);
                if (company.getNumberOfImpression() > capacity) {
                    continue;
                }
                int currentRevenue = revenues[capacity - company.getNumberOfImpression()] + company.getRevenue();
                if (maxCurrentRevenue < currentRevenue) {
                    maxCurrentRevenue = currentRevenue;
                    acceptedCompanies[capacity] = i;
                    revenues[capacity] = maxCurrentRevenue;
                }
            }
            if (maxRevenue < maxCurrentRevenue) {
                maxRevenue = maxCurrentRevenue;
                maxCapacity = capacity;
            }
        }
        return new Result(revenues, acceptedCompanies, maxRevenue, maxCapacity);
    }

    protected Result combine(Result r1, Result r2) {
        int[] combinedRevenues = new int[r1.revenues.length];
        int[] combinedCompanies = new int[r1.revenues.length];
        int maxRevenue = 0;
        int maxCapacity = 0;
        for (int cap = 1; cap < combinedRevenues.length; ++cap) {
            int l = 0;
            int r = cap;
            int maxCurrentRevenue = 0;
            while (l <= cap && r >= 0) {
                int newRevenue = r1.revenues[l] + r2.revenues[r];
                if (maxCurrentRevenue < newRevenue) {
                    maxCurrentRevenue = newRevenue;
                }
                l++;
                r--;
            }
            combinedRevenues[cap] = maxCurrentRevenue;
            combinedCompanies[cap] = Math.max(r1.companies[cap], r2.companies[cap]);
            if (maxRevenue < maxCurrentRevenue) {
                maxRevenue = maxCurrentRevenue;
                maxCapacity = cap;
            }
        }
        return new Result(combinedRevenues, combinedCompanies, maxRevenue, maxCapacity);
    }

    protected class Result {
        int[] revenues;
        int[] companies;
        int maxRevenue;
        int totalCapacity;

        public Result(int[] revenues, int[] companies, int maxRevenue, int totalCapacity) {
            this.revenues = revenues;
            this.companies = companies;
            this.maxRevenue = maxRevenue;
            this.totalCapacity = totalCapacity;
        }
    }
}
