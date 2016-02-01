package com.ooyala.challenge.core.impl;

import com.ooyala.challenge.core.Processor;
import com.ooyala.challenge.data.Company;
import com.ooyala.challenge.data.Output;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 */
public class ForkJoinRevenueProcessor extends AbstractParallelProcessor implements Processor {
    @Override
    protected Output compute(List<Company> companies, int availableImpressions) {
        ForkJoinPool pool = new ForkJoinPool();
        Result result = pool.invoke(new SplitTask(1, 3, companies, availableImpressions, 0, companies.size()));
        return translate(result, companies);
    }

    private class SplitTask extends RecursiveTask<Result> {
        private int splitLevel;
        private int maxSplitLevel;
        private List<Company> companies;
        private int availableImpressions;
        int from;
        int to;

        public SplitTask(int splitLevel, int maxSplitLevel, List<Company> companies, int availableImpressions, int from, int to) {
            this.splitLevel = splitLevel;
            this.maxSplitLevel = maxSplitLevel;
            this.companies = companies;
            this.availableImpressions = availableImpressions;
            this.from = from;
            this.to = to;
        }

        @Override
        protected Result compute() {
            if (splitLevel == maxSplitLevel) {
                return internalCompute(companies, availableImpressions, from, to);
            } else {
                int mid = from + (to - from) / 2;
                SplitTask left = new SplitTask(splitLevel + 1, maxSplitLevel, companies, availableImpressions, from, mid);
                SplitTask right = new SplitTask(splitLevel + 1, maxSplitLevel, companies, availableImpressions, mid, to);
                left.fork();
                right.fork();
                try {
                    Result lResult = left.get();
                    Result rResult = right.get();
                    return combine(lResult, rResult);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }
}
