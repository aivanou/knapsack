package com.ooyala.challenge.core.impl;

import com.ooyala.challenge.core.Processor;
import com.ooyala.challenge.data.Company;
import com.ooyala.challenge.data.Output;
import com.ooyala.challenge.data.OutputItem;
import com.ooyala.challenge.data.OutputMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    /**
     * Note: This method will produce the list of companies that have number of campains greater than zero
     * <p>
     * The algorithm tries to parallelise the knapsack problem:
     * 1. It splits the task onto n subtasks and sends them to workers.
     * 2. After worker finished processing its task, it will add the result into the output queue.
     * 3. The result contains the array[1:max_impressions] and for each impression, the object contains list
     * of companies that produce this impression number. (This requires a lot of additional space,
     * but it is necessary for a combine step)
     * <p>
     * 4. On the combine step, the worker takes two tasks from a queue and tries to find an optimal solution
     * for their combination. It is quite tricky to do
     * (because for each impression it can be any combination of values from result1 and result2.
     * e.g.
     * For computing an optimal value for a value 5, there can be next possibilities:
     * r1 0 1 2 3 4 5
     * <p>
     * r2 5 4 3 2 1 0
     * <p>
     * As a result, in order to find an optimal value, it is required to go through all impressions until current
     * So the complexity of this procedure is really really bad:
     * (0+1+2+..+T) = T^2/2 = O(T^2)
     * )
     * When all results are combined, the answer is put into result queue
     */
    @Override protected Output compute(List<Company> companies, int availableImpressions) {
        Segment[] tasksData = split(companies, nTasks);
        BlockingDeque<Result> outputQueue = new LinkedBlockingDeque<>(nTasks + 2);
        BlockingDeque<Result> resultQueue = new LinkedBlockingDeque<>(1);
        AtomicInteger processingTasks = new AtomicInteger(0);
        CountDownLatch barrier = new CountDownLatch(nTasks);

        for (int i = 0; i < nTasks; ++i) {
            executor.execute(new ComputeTask(companies, tasksData[i], availableImpressions, barrier, resultQueue, outputQueue, processingTasks));
        }
        Result result = null;
        try {
            result = resultQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<OutputItem> items = gatherCompanies(result, companies);
        return new Output(items, new OutputMetadata(result.totalCapacity, result.maxRevenue));
    }

    private List<OutputItem> gatherCompanies(Result result, List<Company> companies) {
        List<OutputItem> items = new ArrayList<>(companies.size());
        items.addAll(companies.stream()
            .map(company -> new OutputItem(company.getName(), 0, 0, 0))
            .collect(Collectors.toList()));
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

    private void increaseOutputValues(OutputItem outputItem, Company company) {
        outputItem.incCampains();
        outputItem.increaseTotalImpressions(company.getNumberOfImpression());
        outputItem.increaseTotalRevenue(company.getRevenue());
    }

    private Segment[] split(List<Company> companies, int nTasks) {
        Segment[] taskData = new Segment[nTasks];
        int batch = companies.size() / nTasks;
        for (int i = 0; i < nTasks - 1; ++i) {
            int left = i * batch;
            int right = i * batch + batch;
            taskData[i] = new Segment(left, right);
        }
        taskData[nTasks - 1] = new Segment((nTasks - 1) * batch, companies.size());
        return taskData;
    }

    private Result internalCompute(List<Company> companies, int availableImpressions, Segment segment) {
        int[] revenues = new int[availableImpressions + 1];
        int maxCapacity = 0;
        int maxRevenue = 0;
        int[] acceptedCompanies = new int[availableImpressions + 1];
        for (int capacity = 1; capacity <= availableImpressions; ++capacity) {
            int maxCurrentRevenue = 0;
            acceptedCompanies[capacity] = -1;
            for (int i = segment.left; i < segment.right; ++i) {
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

    private final Object lock = new Object();

    private class ComputeTask implements Runnable {
        private List<Company> companies;
        private Segment segment;
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

        public ComputeTask(List<Company> companies, Segment segment, int availableImpressions, CountDownLatch barrier,
            BlockingDeque<Result> resultQueue, BlockingDeque<Result> outputQueue, AtomicInteger processingTasks) {
            this.companies = companies;
            this.segment = segment;
            this.availableImpressions = availableImpressions;
            this.barrier = barrier;
            this.resultQueue = resultQueue;
            this.outputQueue = outputQueue;
            this.processingTasks = processingTasks;
        }

        @Override public void run() {
            Result result = internalCompute(companies, availableImpressions, segment);
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
                            return;
                        } else if (outputQueue.isEmpty()) {
                            /**some other workers executing combine step*/
                            outputQueue.add(r1);
                            return;
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
    }

    private class Result {
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

    private class Segment {
        int left;
        int right;

        public Segment(int left, int right) {
            this.left = left;
            this.right = right;
        }
    }
}
