package org.apache.ignite.internal.processors.query.stat;

import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.processors.query.stat.messages.StatisticsKeyMessage;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Collector which scan local data to gather statistics.
 */
public interface StatisticsGathering {
    /**
     * Collect local statistics by specified keys and partitions
     * and pass it to router to send in response to specified reqId.
     *
     * @param reqId Request id.
     * @param keys Keys to collect statistics by.
     * @param parts Partitions to collect statistics by.
     * @param cancelled Supplier to track cancelled state.
     */
    public void collectLocalObjectsStatisticsAsync(
            UUID reqId,
            Set<StatisticsKeyMessage> keys,
            int[] parts,
            Supplier<Boolean> cancelled
    );

    /**
     * Aggregate specified partition level statistics to local level statistics.
     *
     * @param keyMsg Aggregation key.
     * @param stats Collection of all local partition level or local level statistics by specified key to aggregate.
     * @return Local level aggregated statistics.
     */
    public ObjectStatisticsImpl aggregateLocalStatistics(
            StatisticsKeyMessage keyMsg,
            Collection<? extends ObjectStatisticsImpl> stats
    );
}
