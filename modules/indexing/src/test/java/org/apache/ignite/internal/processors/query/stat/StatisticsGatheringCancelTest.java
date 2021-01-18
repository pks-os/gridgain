/*
 * Copyright 2021 GridGain Systems, Inc. and Contributors.
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

import org.apache.ignite.internal.util.typedef.internal.U;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Cancel statistics collection test.
 */
public class StatisticsGatheringCancelTest extends StatisticsRestartAbstractTest {
    /**
     *
     * @return
     * @throws Exception
     */
    private StatisticsTarget[] generateTargets() throws Exception {
        grid(0).getOrCreateCache(DEFAULT_CACHE_NAME);

        int tCount = 10;
        StatisticsTarget[] targets = new StatisticsTarget[tCount];
        for (int i = 0; i < tCount; i++) {
            String tName = "TEST_TABLE" + i;
            runSql("DROP TABLE IF EXISTS " + tName);
            runSql(String.format("CREATE TABLE %s (a INT PRIMARY KEY, b INT, c INT)", tName));
            targets[i] = new StatisticsTarget(SCHEMA, tName);
        }
        return targets;
    }

    /**
     * 1) Create N tables.
     * 2) Start gathering by all created tables in one call.
     * 3) Cancel gathering from the last to the first.
     *
     * @throws Exception In case of errors.
     */
    @Test
    public void cancelGathering() throws Exception {
        IgniteStatisticsManagerImpl statMgr1 = (IgniteStatisticsManagerImpl)grid(1).context().query().getIndexing()
            .statsManager();
        StatisticsTarget[] targets = generateTargets();

        cancelGathering(targets, true, true);

        statMgr1.collectObjectStatistics(targets[0]);

        cancelGathering(targets, false, true);

        statMgr1.collectObjectStatistics(targets[0]);

        cancelGathering(targets, false, false);

        statMgr1.collectObjectStatistics(targets[0]);

        U.sleep(100);

        checkStatTasksEmpty(0);
        checkStatTasksEmpty(1);
    }

    /**
     *
     * @param targets Targets to collect statistics by.
     * @param hangLoc If {@code true} - hand local node, otherwise - hang pool in remote one.
     * @param hangGathering If {@code true} - hang gathering pool, otherwise - hand message processing one.
     * @throws Exception
     */
    private void cancelGathering(StatisticsTarget[] targets, boolean hangLoc, boolean hangGathering) throws Exception {
        IgniteStatisticsManagerImpl statMgr0 = (IgniteStatisticsManagerImpl)grid(0).context().query().getIndexing()
            .statsManager();

        Lock lock;
        if (hangGathering)
            lock = nodeGathLock(hangLoc ? 0 : 1);
        else
            lock = nodeMsgsLock(hangLoc ? 0 : 1);

        StatisticsGatheringFuture<Map<StatisticsTarget, ObjectStatistics>>[] futures = statMgr0
            .collectObjectStatisticsAsync(targets);

        Assert.assertEquals(targets.length, futures.length);

        boolean cancelled = false;
        for (int i = targets.length - 1; i >= 0; i--)
            cancelled = statMgr0.cancelObjectStatisticsGathering(futures[i].gatId()) || cancelled;

        Assert.assertTrue(cancelled);

        lock.unlock();

    }

    /** {@inheritDoc} */
    @Override public int nodes() {
        return 2;
    }
}
