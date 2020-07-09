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

package org.apache.ignite.yardstick.cache.model;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

/**
 * Value used for indexed put test.
 */
public class History {
    /** Value 1. */
    @QuerySqlField(index = true)
    private long tid;

    /** Value 2. */
    @QuerySqlField(index = true)
    private long bid;

    /** Value 3. */
    @QuerySqlField(index = true)
    private long aid;

    /** Value 4. */
    @QuerySqlField(index = true)
    private long delta;

    /**
     * Constructs.
     *
     * @param tid Indexed value.
     */
    public History(long tid, long bid, long aid, long delta) {
        this.tid = tid;
        this.bid = bid;
        this.aid = aid;
        this.delta = delta;
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        Long result = tid;

        result = 31 * result + bid;
        result = 31 * result + aid;
        result = 31 * result + delta;

        return result.intValue();
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return "History [tid=" + tid + ", bid=" + bid + ", aid=" + aid + ", delta=" + delta + ']';
    }
}