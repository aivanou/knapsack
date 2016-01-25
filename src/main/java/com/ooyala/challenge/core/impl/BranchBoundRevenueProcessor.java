package com.ooyala.challenge.core.impl;

import com.ooyala.challenge.core.Processor;
import com.ooyala.challenge.data.Company;
import com.ooyala.challenge.data.Output;
import com.ooyala.challenge.data.OutputItem;
import com.ooyala.challenge.data.OutputMetadata;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class BranchBoundRevenueProcessor extends RevenueProcessor implements Processor {
    @Override
    protected Output compute(List<Company> companies, int availableImpressions) {
        sortCompanies(companies);
        int[] knapsack = new int[companies.size()];
        int[] optimalKnapsack = new int[companies.size()];
        int[] data = scatter(knapsack, 0, availableImpressions, companies);
        int currRevenue = data[0];
        int maxRevenue = currRevenue;
        int currWeight = data[1];
        int totalWeight = currWeight;
        int k = data[2];
        while (true) {
            int availableWeight = availableImpressions - currWeight;
            if (k == -1) {
                break;
            }
            availableWeight += companies.get(k).getNumberOfImpression();
            knapsack[k] -= 1;
            currRevenue -= companies.get(k).getRevenue();
            currWeight -= companies.get(k).getNumberOfImpression();
            data = scatter(knapsack, k + 1, availableWeight, companies);
            currRevenue += data[0];
            currWeight += data[1];
            k = data[2];
            if (currRevenue > maxRevenue) {
                maxRevenue = currRevenue;
                totalWeight = currWeight;
                System.arraycopy(knapsack, 0, optimalKnapsack, 0, companies.size());
            }
        }
        OutputMetadata metadata = new OutputMetadata(totalWeight, maxRevenue);
        List<OutputItem> items = new ArrayList<>(companies.size());
        for (int i = 0; i < optimalKnapsack.length; ++i) {
            Company company = companies.get(i);
            int ncampains = optimalKnapsack[i];
            OutputItem item = new OutputItem(company.getName(), ncampains, ncampains * company.getNumberOfImpression(), ncampains * company.getRevenue());
            items.add(item);
        }
        return new Output(items, metadata);
    }

    private int[] scatter(int[] cmp, int startIndex, int maxImpressions, List<Company> companies) {
        int revenue = 0;
        int usedWeight = 0;
        int k = -1;
        for (int i = startIndex; i < cmp.length; ++i) {
            if (maxImpressions == 0) {
                break;
            }
            Company company = companies.get(i);
            cmp[i] = maxImpressions / company.getNumberOfImpression();
            revenue += cmp[i] * company.getRevenue();
            usedWeight += cmp[i] * company.getNumberOfImpression();
            maxImpressions = maxImpressions % company.getNumberOfImpression();
            if (cmp[i] != 0) {
                k = i;
            }
        }
        if (k == -1) {
            for (int i = Math.min(startIndex, cmp.length - 1); i >= 0; i--) {
                if (cmp[i] != 0) {
                    k = i;
                    break;
                }
            }
        }
        return new int[] { revenue, usedWeight, k };
    }

    private void sortCompanies(List<Company> companies) {
        companies.sort((o1, o2) -> {
            if (o1 == null)
                return -1;
            else if (o2 == null)
                return 1;
            else {
                double v1 = (double) o1.getRevenue() / (double) o1.getNumberOfImpression();
                double v2 = (double) o2.getRevenue() / (double) o2.getNumberOfImpression();
                return Double.compare(v1, v2);
            }
        });
    }
}
