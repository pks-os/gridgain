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

import org.apache.ignite.IgniteCheckedException;
import org.junit.Test;

/**
 * Tests for statistics storage.
 */
public abstract class StatisticsStorageTest extends StatisticsStorageAbstractTest {
    /**
     * Test that statistics manager will return local statistics after cleaning of statistics repository.
     * @throws IgniteCheckedException In case of errors.
     */
    @Test
    public void clearAllTest() throws IgniteCheckedException {
        IgniteStatisticsManager statsManager = grid(0).context().query().getIndexing().statsManager();
        IgniteStatisticsRepositoryImpl statsRepository = (IgniteStatisticsRepositoryImpl)
                ((IgniteStatisticsManagerImpl)statsManager).statisticsRepository();
        IgniteStatisticsStore statsStore = statsRepository.statisticsStore();

        statsStore.clearAllStatistics();

        ObjectStatisticsImpl localStat = (ObjectStatisticsImpl) statsManager
                .getLocalStatistics("PUBLIC", "SMALL");
        assertNotNull(localStat);

        statsManager.collectObjectStatistics("PUBLIC", "SMALL");
    }

    /**
     * Collect statistics by whole table twice and compare collected statistics:
     * 1) collect statistics by table and get it.
     * 2) collect same statistics by table again and get it.
     * 3) compare obtained statistics.
     */
    @Test
    public void testRecollection() throws Exception {
        IgniteStatisticsManager statsManager = grid(0).context().query().getIndexing().statsManager();
        statsManager.collectObjectStatistics("PUBLIC", "SMALL");
        ObjectStatisticsImpl localStat = (ObjectStatisticsImpl) statsManager
                .getLocalStatistics("PUBLIC", "SMALL");
        statsManager.collectObjectStatistics("PUBLIC", "SMALL");
        ObjectStatisticsImpl localStat2 = (ObjectStatisticsImpl) statsManager
                .getLocalStatistics("PUBLIC", "SMALL");

        assertEquals(localStat, localStat2);
    }

    /**
     * Collect statistics partially twice and compare collected statistics:
     * 1) collect statistics by column and get it.
     * 2) collect same statistics by column again and get it.
     * 3) compare obtained statistics.
     *
     */
    @Test
    public void testPartialRecollection() throws Exception {
        IgniteStatisticsManager statsManager = grid(0).context().query().getIndexing().statsManager();
        statsManager.collectObjectStatistics("PUBLIC", "SMALL", "B");
        ObjectStatisticsImpl localStat = (ObjectStatisticsImpl) statsManager
                .getLocalStatistics("PUBLIC", "SMALL");
        statsManager.collectObjectStatistics("PUBLIC", "SMALL", "B");
        ObjectStatisticsImpl localStat2 = (ObjectStatisticsImpl) statsManager
                .getLocalStatistics("PUBLIC", "SMALL");

        assertEquals(localStat, localStat2);
    }

    /**
     * Clear statistics twice and check that .
     */
    @Test
    public void testDoubleDeletion() throws Exception {
        IgniteStatisticsManager statsManager = grid(0).context().query().getIndexing().statsManager();

        statsManager.clearObjectStatistics("PUBLIC", "SMALL");
        ObjectStatisticsImpl localStat = (ObjectStatisticsImpl) statsManager
                .getLocalStatistics("PUBLIC", "SMALL");
        assertNull(localStat);

        statsManager.clearObjectStatistics("PUBLIC", "SMALL");
        ObjectStatisticsImpl localStat2 = (ObjectStatisticsImpl) statsManager
                .getLocalStatistics("PUBLIC", "SMALL");
        assertNull(localStat2);

        statsManager.collectObjectStatistics("PUBLIC", "SMALL");
    }

    /**
     * Clear statistics partially twice.
     */
    @Test
    public void testDoublePartialDeletion() throws Exception {
        IgniteStatisticsManager statsManager = grid(0).context().query().getIndexing().statsManager();

        statsManager.clearObjectStatistics("PUBLIC", "SMALL", "B");

        ObjectStatisticsImpl localStat = (ObjectStatisticsImpl) statsManager
                .getLocalStatistics("PUBLIC", "SMALL");
        assertNotNull(localStat);
        assertNotNull(localStat.columnsStatistics().get("A"));
        assertNull(localStat.columnsStatistics().get("B"));

        statsManager.clearObjectStatistics("PUBLIC", "SMALL", "B");

        ObjectStatisticsImpl localStat2 = (ObjectStatisticsImpl) statsManager
                .getLocalStatistics("PUBLIC", "SMALL");
        assertNotNull(localStat2);
        assertNotNull(localStat.columnsStatistics().get("A"));
        assertNull(localStat.columnsStatistics().get("B"));

        statsManager.collectObjectStatistics("PUBLIC", "SMALL");
    }
}
