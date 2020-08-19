/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
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

package org.apache.ignite.internal.processors.cache;

import java.util.Collections;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMetrics;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheRebalanceMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.events.Event;
import org.apache.ignite.events.EventType;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.util.lang.GridAbsPredicate;
import org.apache.ignite.internal.util.typedef.PA;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.internal.visor.VisorTaskArgument;
import org.apache.ignite.internal.visor.node.VisorNodeDataCollectorTask;
import org.apache.ignite.internal.visor.node.VisorNodeDataCollectorTaskArg;
import org.apache.ignite.internal.visor.node.VisorNodeDataCollectorTaskResult;
import org.apache.ignite.lang.IgnitePredicate;
import org.apache.ignite.testframework.GridTestUtils;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;

import static org.apache.ignite.IgniteSystemProperties.IGNITE_REBALANCE_STATISTICS_TIME_INTERVAL;
import static org.apache.ignite.testframework.GridTestUtils.runAsync;
import static org.apache.ignite.testframework.GridTestUtils.waitForCondition;

/**
 *
 */
public class CacheGroupsMetricsRebalanceTest extends GridCommonAbstractTest {
    /** */
    private static final String CACHE1 = "cache1";

    /** */
    private static final String CACHE2 = "cache2";

    /** */
    private static final String CACHE3 = "cache3";

    /** */
    private static final long REBALANCE_DELAY = 5_000;

    /** */
    private static final String GROUP = "group1";

    /** Acceptable time inaccuracy for testRebalanceEstimateFinishTime() */
    public static final long ACCEPTABLE_TIME_INACCURACY = 25_000L;

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        super.afterTest();

        stopAllGrids();
    }

    /** {@inheritDoc} */
    @Override protected long getTestTimeout() {
        return 10 * 60 * 1000;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(igniteInstanceName);

        CacheConfiguration cfg1 = new CacheConfiguration()
            .setName(CACHE1)
            .setGroupName(GROUP)
            .setCacheMode(CacheMode.PARTITIONED)
            .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
            .setRebalanceMode(CacheRebalanceMode.ASYNC)
            .setRebalanceBatchSize(100)
            .setStatisticsEnabled(true);

        CacheConfiguration cfg2 = new CacheConfiguration(cfg1)
            .setName(CACHE2);

        cfg.setCacheConfiguration(cfg1, cfg2);

        cfg.setIncludeEventTypes(EventType.EVTS_ALL);

        return cfg;
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testRebalance() throws Exception {
        Ignite ignite = startGrids(4);

        IgniteCache<Object, Object> cache1 = ignite.cache(CACHE1);
        IgniteCache<Object, Object> cache2 = ignite.cache(CACHE2);

        for (int i = 0; i < 10000; i++) {
            cache1.put(i, CACHE1 + "-" + i);

            if (i % 2 == 0)
                cache2.put(i, CACHE2 + "-" + i);
        }

        final CountDownLatch l1 = new CountDownLatch(1);
        final CountDownLatch l2 = new CountDownLatch(1);

        startGrid(4).events().localListen(new IgnitePredicate<Event>() {
            @Override public boolean apply(Event evt) {
                l1.countDown();

                try {
                    assertTrue(l2.await(5, TimeUnit.SECONDS));
                }
                catch (InterruptedException e) {
                    throw new AssertionError();
                }

                return false;
            }
        }, EventType.EVT_CACHE_REBALANCE_STOPPED);

        assertTrue(l1.await(5, TimeUnit.SECONDS));

        ignite = ignite(4);

        CacheMetrics metrics1 = ignite.cache(CACHE1).localMetrics();
        CacheMetrics metrics2 = ignite.cache(CACHE2).localMetrics();

        l2.countDown();

        long rate1 = metrics1.getRebalancingKeysRate();
        long rate2 = metrics2.getRebalancingKeysRate();

        assertTrue(rate1 > 0);
        assertTrue(rate2 > 0);

        // rate1 has to be roughly the same as rate2
        double ratio = ((double)rate2 / rate1);

        log.info("Ratio: " + ratio);

        assertTrue(ratio > 0.9 && ratio < 1.1);
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testRebalanceProgressUnderLoad() throws Exception {
        Ignite ignite = startGrids(4);

        IgniteCache<Object, Object> cache1 = ignite.cache(CACHE1);

        Random r = new Random();

        GridTestUtils.runAsync(new Runnable() {
            @Override public void run() {
                for (int i = 0; i < 100_000; i++) {
                    int next = r.nextInt();

                    cache1.put(next, CACHE1 + "-" + next);
                }
            }
        });

        IgniteEx ig = startGrid(4);

        GridTestUtils.runAsync(new Runnable() {
            @Override public void run() {
                for (int i = 0; i < 100_000; i++) {
                    int next = r.nextInt();

                    cache1.put(next, CACHE1 + "-" + next);
                }
            }
        });

        CountDownLatch latch = new CountDownLatch(1);

        ig.events().localListen(new IgnitePredicate<Event>() {
            @Override public boolean apply(Event evt) {
                latch.countDown();

                return false;
            }
        }, EventType.EVT_CACHE_REBALANCE_STOPPED);

        latch.await();

        VisorNodeDataCollectorTaskArg taskArg = new VisorNodeDataCollectorTaskArg();
        taskArg.setCacheGroups(Collections.emptySet());

        VisorTaskArgument<VisorNodeDataCollectorTaskArg> arg = new VisorTaskArgument<>(
            Collections.singletonList(ignite.cluster().localNode().id()),
            taskArg,
            false
        );

        GridTestUtils.waitForCondition(new GridAbsPredicate() {
            @Override public boolean apply() {
                VisorNodeDataCollectorTaskResult res = ignite.compute().execute(VisorNodeDataCollectorTask.class, arg);

                CacheMetrics snapshot = ig.cache(CACHE1).metrics();

                return snapshot.getRebalancedKeys() > snapshot.getEstimatedRebalancingKeys()
                    && Double.compare(res.getRebalance().get(ignite.cluster().localNode().id()), 1.0) == 0
                    && snapshot.getRebalancingPartitionsCount() == 0;
            }
        }, 5000);
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testRebalanceEstimateFinishTime() throws Exception {
        System.setProperty(IGNITE_REBALANCE_STATISTICS_TIME_INTERVAL, String.valueOf(10_000));

        Ignite ig1 = startGrid(1);

        final int KEYS = GridTestUtils.SF.applyLB(1_000_000, 300_000);

        try (IgniteDataStreamer<Integer, String> st = ig1.dataStreamer(CACHE1)) {
            for (int i = 0; i < KEYS; i++)
                st.addData(i, CACHE1 + "-" + i);
        }

        final Ignite ig2 = startGrid(2);

        boolean rebalancingStartTimeGot = waitForCondition(() -> ig2.cache(CACHE1).localMetrics().getRebalancingStartTime() != -1L, 5_000);

        assertTrue("Unable to resolve rebalancing start time.", rebalancingStartTimeGot);

        CacheMetrics metrics = ig2.cache(CACHE1).localMetrics();

        long startTime = metrics.getRebalancingStartTime();
        long currTime = U.currentTimeMillis();

        assertTrue("Invalid start time [startTime=" + startTime + ", currTime=" + currTime + ']',
            startTime > 0L && (currTime - startTime) >= 0L && (currTime - startTime) <= 5000L);

        final CountDownLatch latch = new CountDownLatch(1);

        runAsync(() -> {
            // Waiting 75% keys will be rebalanced.
            int partKeys = KEYS / 2;

            final long keysLine = (long)partKeys / 4L;

            log.info("Wait until keys left will be less than: " + keysLine);

            while (true) {
                CacheMetrics m = ig2.cache(CACHE1).localMetrics();

                long keyLeft = m.getKeysToRebalanceLeft();

                if (keyLeft < keysLine) {
                    latch.countDown();

                    break;
                }

                log.info("Keys left: " + m.getKeysToRebalanceLeft());

                try {
                    Thread.sleep(1_000);
                }
                catch (InterruptedException e) {
                    log.warning("Interrupt thread", e);

                    Thread.currentThread().interrupt();
                }
            }
        });

        assertTrue(latch.await(getTestTimeout(), TimeUnit.MILLISECONDS));

        boolean estimatedRebalancingFinishTimeGot = waitForCondition(new PA() {
            @Override public boolean apply() {
                return ig2.cache(CACHE1).localMetrics().getEstimatedRebalancingFinishTime() != -1L;
            }
        }, 5_000L);

        assertTrue("Unable to resolve estimated rebalancing finish time.", estimatedRebalancingFinishTimeGot);

        long finishTime = ig2.cache(CACHE1).localMetrics().getEstimatedRebalancingFinishTime();

        assertTrue("Not a positive estimation of rebalancing finish time: " + finishTime,
            finishTime > 0L);

        currTime = U.currentTimeMillis();

        long timePassed = currTime - startTime;
        long timeLeft = finishTime - currTime;

        // TODO: finishRebalanceLatch gets countdown much earlier because of ForceRebalanceExchangeTask triggered by cache with delay
//        assertTrue("Got timeout while waiting for rebalancing. Estimated left time: " + timeLeft,
//            finishRebalanceLatch.await(timeLeft + 10_000L, TimeUnit.MILLISECONDS));

        boolean allKeysRebalanced = waitForCondition(new GridAbsPredicate() {
            @Override public boolean apply() {
                return ig2.cache(CACHE1).localMetrics().getKeysToRebalanceLeft() == 0;
            }
        }, timeLeft + ACCEPTABLE_TIME_INACCURACY);

        assertTrue("Some keys aren't rebalanced.", allKeysRebalanced);

        log.info("[timePassed=" + timePassed + ", timeLeft=" + timeLeft +
                ", Time to rebalance=" + (finishTime - startTime) +
                ", startTime=" + startTime + ", finishTime=" + finishTime + ']'
        );

        System.clearProperty(IGNITE_REBALANCE_STATISTICS_TIME_INTERVAL);

        currTime = U.currentTimeMillis();

        log.info("Rebalance time: " + (currTime - startTime));

        long diff = finishTime - currTime;

        assertTrue("Expected less than " + ACCEPTABLE_TIME_INACCURACY + ", but actual: " + diff,
            Math.abs(diff) < ACCEPTABLE_TIME_INACCURACY);
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testRebalanceDelay() throws Exception {
        Ignite ig1 = startGrid(1);

        CacheConfiguration cfg3 = new CacheConfiguration()
            .setName(CACHE3)
            .setCacheMode(CacheMode.PARTITIONED)
            .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
            .setRebalanceMode(CacheRebalanceMode.ASYNC)
            .setRebalanceBatchSize(100)
            .setStatisticsEnabled(true)
            .setRebalanceDelay(REBALANCE_DELAY);

        final IgniteCache<Object, Object> cache = ig1.getOrCreateCache(cfg3);

        for (int i = 0; i < 10000; i++)
            cache.put(i, CACHE3 + "-" + i);

        long beforeStartTime = U.currentTimeMillis();

        startGrid(2);
        startGrid(3);

        waitForCondition(new PA() {
            @Override public boolean apply() {
                return cache.localMetrics().getRebalancingStartTime() != -1L;
            }
        }, 5_000);

        assert (cache.localMetrics().getRebalancingStartTime() < U.currentTimeMillis() + REBALANCE_DELAY);
        assert (cache.localMetrics().getRebalancingStartTime() > beforeStartTime + REBALANCE_DELAY);
    }
}
