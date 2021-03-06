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

import java.util.Iterator;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.util.GridLongList;
import org.jetbrains.annotations.Nullable;

/**
 * Partition update counter maintains three entities for tracking partition update state.
   <ol>
 *     <li><b>Low water mark (LWM)</b> or update counter - lowest applied sequential update number.</li>
 *     <li><b>High water mark (HWM)</b> or reservation counter - highest seen but unapplied yet update number.</li>
 *     <li>Out-of-order applied updates in range between LWM and HWM.</li>
 * </ol>
 */
public interface PartitionUpdateCounter extends Iterable<long[]> {
    /**
     * Restores update counter state.
     *
     * @param initUpdCntr LWM.
     * @param updCntrGapsData Updates counters gaps raw data.
     */
    public void init(long initUpdCntr, @Nullable byte[] updCntrGapsData);

    /**
     * @deprecated TODO LWM should be used as initial counter https://ggsystems.atlassian.net/browse/GG-17396
     */
    public long initial();

    /**
     * Get LWM.
     *
     * @return Current LWM.
     */
    public long get();

    /**
     * Increment LWM by 1.
     *
     * @return New LWM.
     */
    public long next();

    /**
     * Increment LWM by delta.
     *
     * @param delta Delta.
     * @return New LWM.
     */
    public long next(long delta);

    /**
     * Increment HWM by delta.
     *
     * @param delta Delta.
     * @return New HWM.
     */
    public long reserve(long delta);

    /**
     * Returns HWM.
     * @return Current HWM.
     */
    public long reserved();

    /**
     * Sets update counter to absolute value. All missed updates will be discarded.
     *
     * @param val Absolute value.
     * @throws IgniteCheckedException if counter cannot be set to passed value due to incompatibility with current state.
     */
    public void update(long val) throws IgniteCheckedException;

    /**
     * Applies counter update out of range. Update ranges must not intersect.
     *
     * @param start Start (<= lwm).
     * @param delta Delta.
     * @return {@code True} if update was actually applied.
     */
    public boolean update(long start, long delta);

    /**
     * Reset counter internal state to zero.
     */
    public void reset();

    /**
     * Reset the initial counter value to zero.
     */
    public void resetInitialCounter();

    /**
     * @param start Counter.
     * @param delta Delta.
     * @deprecated TODO https://ggsystems.atlassian.net/browse/GG-17396
     */
    public void updateInitial(long start, long delta);

    /**
     * Flushes pending update counters closing all possible gaps.
     *
     * @return Even-length array of pairs [start, end] for each gap.
     */
    public GridLongList finalizeUpdateCounters();

    /** */
    public @Nullable byte[] getBytes();

    /**
     * @return {@code True} if counter has no missed updates.
     */
    public boolean sequential();

    /**
     * @return {@code True} if counter has not seen any update.
     */
    public boolean empty();

    /**
     * @return Iterator for pairs [start, range] for each out-of-order update in the update counter sequence.
     */
    @Override public Iterator<long[]> iterator();

    /**
     * @return Cache group context.
     */
    public CacheGroupContext context();

    /**
     * Update tombstone clear counter. Should be called when a tombstone is removed.
     * @param cntr New counter value.
     */
    public void updateTombstoneClearCounter(long cntr);

    /**
     * Returns the clear counter - some tombstones up to this counter can be removed, later tombstones are not cleared.
     *
     * @return Counter value.
     */
    public long tombstoneClearCounter();
}
