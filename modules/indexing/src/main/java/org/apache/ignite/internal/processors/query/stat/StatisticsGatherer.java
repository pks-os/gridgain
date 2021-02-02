/*
 * Copyright 2020 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ignite.internal.processors.query.stat;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.apache.ignite.IgniteLogger;
import org.apache.ignite.internal.processors.query.h2.opt.GridH2Table;
import org.apache.ignite.internal.processors.query.stat.task.CollectPartitionStatistics;
import org.apache.ignite.thread.IgniteThreadPoolExecutor;
import org.gridgain.internal.h2.table.Column;

/**
 * Implementation of statistic collector.
 */
public class StatisticsGatherer {
    /** Logger. */
    private final IgniteLogger log;

    /** Ignite statistics repository. */
    private final IgniteStatisticsRepository statRepo;

    /** Ignite Thread pool executor to do statistics collection tasks. */
    private final IgniteThreadPoolExecutor gatherPool;

    /** (cacheGroupId -> gather context) */
    private final ConcurrentMap<StatisticsKey, LocalStatisticsGatheringContext> gatheringInProgress = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param repo IgniteStatisticsRepository.
     * @param reqProc Statistics request crawler.
     * @param gatherPool Thread pool to gather statistics in.
     * @param logSupplier Log supplier function.
     */
    public StatisticsGatherer(
        IgniteStatisticsRepository repo,
        StatisticsRequestProcessor reqProc,
        IgniteThreadPoolExecutor gatherPool,
        Function<Class<?>, IgniteLogger> logSupplier
    ) {
        this.log = logSupplier.apply(StatisticsGatherer.class);
        this.statRepo = repo;
        this.gatherPool = gatherPool;
    }

    /**
     * Collect statistic per partition for specified objects on the same cache group.
     */
    public CompletableFuture<Void> collectLocalObjectsStatisticsAsync(
        GridH2Table tbl,
        Column[] cols,
        Set<Integer> parts,
        long ver
    ) {
        StatisticsKey key = new StatisticsKey(tbl.getSchema().getName(), tbl.getName());

        if (log.isDebugEnabled()) {
            log.debug("Start statistics gathering [key=" + key +
                ", cols=" + Arrays.toString(cols) +
                ", parts=" + parts + ']');
        }

        final LocalStatisticsGatheringContext newCtx = new LocalStatisticsGatheringContext(parts);

        LocalStatisticsGatheringContext oldCtx = gatheringInProgress.put(key, newCtx);

        if (oldCtx != null) {
            if (log.isDebugEnabled())
                log.debug("Cancel previous statistic gathering for [key=" + key + ']');

            oldCtx.future().cancel(false);
        }

        for (int part : parts) {
            CollectPartitionStatistics task = new CollectPartitionStatistics(
                newCtx, tbl,
                cols,
                part,
                ver,
                log
            );

            CompletableFuture<ObjectPartitionStatisticsImpl> f = CompletableFuture.supplyAsync(task::call, gatherPool);

            f.thenAccept((partStat) -> {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Start saving local partitioned statistic [key=" + key +
                            ", part=" + part + ']');
                    }

                    if (partStat == null) {
                        if (log.isDebugEnabled())
                            log.debug("Gathering was canceled [key=" + key + ", part=" + part + ']');

                        return;
                    }

                    statRepo.saveLocalPartitionStatistics(
                        new StatisticsKey(tbl.getSchema().getName(), tbl.getName()),
                        partStat
                    );

                    if (log.isDebugEnabled()) {
                        log.debug("Save local partitioned statistic done [key=" + key +
                            ", part=" + part + ']');
                    }

                    newCtx.partitionDone(part);
                }
                catch (Throwable ex) {
                    log.error("Unexpected error os statistic save", ex);
                }
            });
        }

        // Wait all partition gathering tasks.
        return newCtx.future();
    }

    /** */
    public void submit(Runnable task) {
        gatherPool.submit(task);
    }
}