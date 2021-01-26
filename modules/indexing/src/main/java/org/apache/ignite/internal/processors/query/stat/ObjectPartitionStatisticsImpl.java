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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.ignite.internal.processors.query.stat.schema.StatisticConfiguration;

/**
 * Statistic for some partition of data object.
 */
public class ObjectPartitionStatisticsImpl extends ObjectStatisticsImpl {
    /** Partition id. */
    private final int partId;

    /** Partition update counter at the moment when statistics collected. */
    private final long updCnt;

    /** Local flag. */
    private final boolean loc;

    /**
     * Constructor.
     *
     * @param partId Partition id.
     * @param loc Local flag.
     * @param rowsCnt Total count of rows in partition.
     * @param updCnt Update counter of partition.
     * @param colNameToStat Column key to column statistics map.
     */
    public ObjectPartitionStatisticsImpl(
            int partId,
            boolean loc,
            long rowsCnt,
            long updCnt,
            Map<String, ColumnStatistics> colNameToStat,
            StatisticConfiguration cfg,
            long ver

    ) {
        super(rowsCnt, colNameToStat, cfg, ver);

        this.partId = partId;
        this.loc = loc;
        this.updCnt = updCnt;
    }

    /**
     * @return Partition id.
     */
    public int partId() {
        return partId;
    }

    /**
     * @return Is local flag.
     */
    public boolean local() {
        return loc;
    }

    /**
     * @return Partition update counter.
     */
    public long updCnt() {
        return updCnt;
    }

    /** {@inheritDoc} */
    @Override public ObjectPartitionStatisticsImpl clone() {
        return new ObjectPartitionStatisticsImpl(partId, loc, rowCount(), updCnt, new HashMap<>(columnsStatistics()), config(), version());
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ObjectPartitionStatisticsImpl that = (ObjectPartitionStatisticsImpl) o;
        return partId == that.partId &&
                updCnt == that.updCnt &&
                loc == that.loc;
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        return Objects.hash(super.hashCode(), partId, updCnt, loc);
    }
}
