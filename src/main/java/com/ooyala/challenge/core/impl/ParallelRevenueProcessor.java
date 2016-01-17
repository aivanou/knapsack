package com.ooyala.challenge.core.impl;

import com.ooyala.challenge.core.Processor;
import com.ooyala.challenge.data.Company;
import com.ooyala.challenge.data.Output;
import com.ooyala.challenge.data.OutputData;
import com.ooyala.challenge.data.OutputMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Parallel implementation of unbounded knapsack problem.
 * <p>
 * The algorithm contains of several steps:
 * <p>
 * 1. divide the task into subtasks
 * <p>
 * 2. compute the local optimal solution
 * <p>
 * 3. combine solutions
 * <p>
 * NOTE:
 * <p>
 * Unfortunately, current implementation of the algorithm consumes a lot of memory.
 * <p>
 * The other solution can be to replace the Capacity class with the Integer array which contains company that was added to produce corresponding amount of
 * impressions. In this case the combine step will be much more difficult to implement. Moreover, on the combine step the two array should be merged which will
 * consume a lot of space(the initial arrays still needs to be kept).
 * <p>
 * The outputQueue contains tasks that needs to be merged
 * <p>
 * The resultQueue contains the final result
 */
public class ParallelRevenueProcessor extends RevenueProcessor implements Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelRevenueProcessor.class);
    private Executor executor;
    private int nTasks;

    public ParallelRevenueProcessor(Executor executor, int nTasks) {
        this.executor = executor;
        this.nTasks = nTasks;
    }

    @Override protected Output compute(List<Company> companies, int availableImpressions) {
        List<Company>[] tasksData = split(companies, nTasks);
        BlockingDeque<Result> outputQueue = new LinkedBlockingDeque<>(nTasks + 2);
        BlockingDeque<Result> resultQueue = new LinkedBlockingDeque<>(1);
        AtomicInteger processingTasks = new AtomicInteger(0);
        CountDownLatch barrier = new CountDownLatch(nTasks);

        for (int i = 0; i < nTasks; ++i) {
            executor.execute(new ComputeTask(tasksData[i], availableImpressions, barrier, resultQueue, outputQueue, processingTasks));
        }
        Result result = null;
        try {
            result = resultQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Map<String, OutputData> reduce = new HashMap<>();
        result.capacities[result.totalCapacity].companies.stream().forEach(
            company -> {
                if (!reduce.containsKey(company.getName())) {
                    reduce.put(company.getName(), new OutputData(company.getName(), 1, company.getNumberOfImpression(), company.getRevenue()));
                } else {
                    OutputData out = reduce.get(company.getName());
                    out.incCampains();
                    out.setTotalImpression(out.getTotalImpression() + company.getNumberOfImpression());
                    out.setTotalRevenue(out.getTotalRevenue() + company.getRevenue());
                }
            });
        return new Output(reduce.values(), new OutputMetadata(result.totalCapacity, result.maxRevenue));
    }

    private List<Company>[] split(List<Company> companies, int nTasks) {
        List<Company>[] taskData = new List[nTasks];
        int batch = companies.size() / nTasks;
        for (int i = 0; i < nTasks - 1; ++i) {
            taskData[i] = new ArrayList<>();
            for (int j = 0; j < batch; ++j) {
                taskData[i].add(companies.get(i * nTasks + j));
            }
        }
        taskData[nTasks - 1] = new ArrayList<>();
        for (int i = 0; i < batch + companies.size() % nTasks; ++i) {
            taskData[nTasks - 1].add(companies.get((nTasks - 1) * batch + i));
        }
        return taskData;
    }

    private Result internalCompute(List<Company> companies, int availableImpressions) {
        Result result = new Result(new Capacity[availableImpressions + 1]);
        result.capacities[0] = new Capacity(new ArrayList<>(), 0);
        int maxCapacity = 0;
        int maxRevenue = 0;
        int[] acceptedCompanies = new int[availableImpressions + 1];
        for (int capacity = 1; capacity <= availableImpressions; ++capacity) {
            result.capacities[capacity] = new Capacity(new ArrayList<>(), 0);
            int maxCurrentRevenue = 0;
            for (int i = 0; i < companies.size(); ++i) {
                Company company = companies.get(i);
                if (company.getNumberOfImpression() > capacity) {
                    continue;
                }
                int currentRevenue = result.capacities[capacity - company.getNumberOfImpression()].revenue + company.getRevenue();
                if (maxCurrentRevenue < currentRevenue) {
                    maxCurrentRevenue = currentRevenue;
                    acceptedCompanies[capacity] = i;
                    result.capacities[capacity].revenue = maxCurrentRevenue;
                }
            }
            if (maxRevenue < maxCurrentRevenue) {
                maxRevenue = maxCurrentRevenue;
                maxCapacity = capacity;
            }
        }
        result.maxRevenue = maxRevenue;
        result.totalCapacity = maxCapacity;
        gatherCompanies(result, companies, acceptedCompanies);
        return result;
    }

    private void gatherCompanies(Result result, List<Company> companies, int[] acceptedCompanies) {
        for (int i = 1; i < acceptedCompanies.length; ++i) {
            gatherCompanies(result, companies, acceptedCompanies, i);
        }
    }

    private void gatherCompanies(Result result, List<Company> companies, int[] acceptedCompanies, int current) {
        Capacity currentCapacity = result.capacities[current];
        while (current > 0) {
            if (!result.capacities[current].companies.isEmpty()) {
                currentCapacity.companies.addAll(result.capacities[current].companies);
                return;
            }
            Company company = companies.get(acceptedCompanies[current]);
            currentCapacity.companies.add(company);
            current -= company.getNumberOfImpression();
        }
    }

    private final Object lock = new Object();

    private class ComputeTask implements Runnable {
        private List<Company> companies;
        private int availableImpressions;
        private BlockingDeque<Result> outputQueue;
        private BlockingDeque<Result> resultQueue;
        private CountDownLatch barrier;
        /**
         * Gentle interruption
         */
        private volatile boolean interrupt = false;
        private final int WAIT_TIME = 2;
        private volatile AtomicInteger processingTasks;

        public ComputeTask(List<Company> companies, int availableImpressions, CountDownLatch barrier,
            BlockingDeque<Result> resultQueue, BlockingDeque<Result> outputQueue, AtomicInteger processingTasks) {
            this.companies = companies;
            this.availableImpressions = availableImpressions;
            this.barrier = barrier;
            this.resultQueue = resultQueue;
            this.outputQueue = outputQueue;
            this.processingTasks = processingTasks;
        }

        @Override public void run() {
            Result result = internalCompute(companies, availableImpressions);
            outputQueue.add(result);
            barrier.countDown();
            waitForAllTasksToFinish();
            if (!interrupt) {
                combine();
            }
        }

        public boolean isInterrupt() {
            return interrupt;
        }

        public void setInterrupt(boolean interrupt) {
            this.interrupt = interrupt;
        }

        private void waitForAllTasksToFinish() {
            try {
                while (!barrier.await(WAIT_TIME, TimeUnit.SECONDS)) {
                    if (interrupt) {
                        LOGGER.warn("TASK was interrupted by somebody");
                        return;
                    }
                }
            } catch (InterruptedException e) {
                //interrupted, finish the task
                LOGGER.error(e.getLocalizedMessage());
                return;
            }
        }

        private void combine() {
            while (true) {
                Result r1 = null;
                Result r2 = null;
                synchronized (lock) {
                    if (outputQueue.isEmpty()) {
                        return;
                    }
                    try {
                        r1 = getObjectFromQueue();
                        if (outputQueue.isEmpty() && processingTasks.get() == 0) {
                            /**if the queue has only 1 element that means that all subtasks were combined*/
                            resultQueue.add(r1);
                        } else {
                            /**some other workers executing combine step*/
                            outputQueue.add(r1);
                            continue;
                        }
                        r2 = getObjectFromQueue();
                        processingTasks.incrementAndGet();
                    } catch (InterruptedException e) {
                        LOGGER.error(e.getLocalizedMessage());
                        return;
                    }
                }
                Result combined = combine(r1, r2);
                outputQueue.add(combined);
                processingTasks.decrementAndGet();
            }
        }

        private Result getObjectFromQueue() throws InterruptedException {
            Result r;
            while ((r = outputQueue.poll(WAIT_TIME, TimeUnit.SECONDS)) == null) {
                if (interrupt) {
                    throw new InterruptedException();
                }
            }
            return r;
        }

        private Result combine(Result r1, Result r2) {
            Result combined = new Result(new Capacity[r1.capacities.length]);
            combined.capacities[0] = new Capacity(new ArrayList<>(), 0);
            int maxRevenue = 0;
            int maxCapacity = 0;
            for (int cap = 1; cap < r1.capacities.length; ++cap) {
                int l = 0;
                int r = cap;
                int maxL = 0;
                int maxR = 0;
                int maxCurrentRevenue = 0;
                while (l <= cap && r >= 0) {
                    int newRevenue = r1.capacities[l].revenue + r2.capacities[r].revenue;
                    if (maxCurrentRevenue < newRevenue) {
                        maxCurrentRevenue = newRevenue;
                        maxL = l;
                        maxR = r;
                    }
                    l++;
                    r--;
                }
                if (maxL == 0) {
                    combined.capacities[cap] = r2.capacities[maxR];
                } else {
                    combined.capacities[cap] = combine(r1.capacities[maxL], r2.capacities[maxR]);
                }
                if (maxRevenue < maxCurrentRevenue) {
                    maxRevenue = maxCurrentRevenue;
                    maxCapacity = cap;
                }
            }
            r1.maxRevenue = maxRevenue;
            r1.totalCapacity = maxCapacity;
            return r1;
        }

        private Capacity combine(Capacity c1, Capacity c2) {
            c1.companies.addAll(c2.companies);
            c1.revenue += c2.revenue;
            return c1;
        }
    }

    private class Result {
        Capacity[] capacities;
        int maxRevenue;
        int totalCapacity;

        public Result(Capacity[] capacities) {
            this.capacities = capacities;
        }
    }

    private class Capacity {
        private List<Company> companies;
        private int revenue;

        public Capacity(List<Company> companies, int capacity) {
            this.companies = companies;
            this.revenue = capacity;
        }
    }
}
