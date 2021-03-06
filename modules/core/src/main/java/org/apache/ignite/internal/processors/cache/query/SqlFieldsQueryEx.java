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

package org.apache.ignite.internal.processors.cache.query;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.internal.processors.query.NestedTxMode;
import org.apache.ignite.internal.util.typedef.F;

/**
 * {@link SqlFieldsQuery} with experimental and internal features.
 */
public final class SqlFieldsQueryEx extends SqlFieldsQuery {
    /** */
    private static final long serialVersionUID = 0L;

    /** Flag to enforce checks for correct operation type. */
    private final Boolean isQry;

    /** Auto commit flag. */
    private boolean autoCommit = true;

    /** Nested transactions handling mode. */
    private NestedTxMode nestedTxMode = NestedTxMode.DEFAULT;

    /** Batched arguments list. */
    private List<Object[]> batchedArgs;

    /** Max memory available for query. */
    private long maxMem;

    /**
     * @param sql SQL query.
     * @param isQry Flag indicating whether this object denotes a query or an update operation.
     */
    public SqlFieldsQueryEx(String sql, Boolean isQry) {
        super(sql);

        this.isQry = isQry;
    }

    /**
     * @param qry SQL query.
     */
    private SqlFieldsQueryEx(SqlFieldsQueryEx qry) {
        super(qry);

        this.isQry = qry.isQry;
        this.autoCommit = qry.autoCommit;
        this.nestedTxMode = qry.nestedTxMode;
        this.batchedArgs = qry.batchedArgs;
        this.maxMem = qry.maxMem;
    }

    /**
     * @return Flag indicating whether this object denotes a query or an update operation.
     */
    public Boolean isQuery() {
        return isQry;
    }

    /** {@inheritDoc} */
    @Override public SqlFieldsQueryEx setSql(String sql) {
        super.setSql(sql);

        return this;
    }

    /** {@inheritDoc} */
    @Override public SqlFieldsQueryEx setArgs(Object... args) {
        super.setArgs(args);

        return this;
    }

    /** {@inheritDoc} */
    @Override public SqlFieldsQueryEx setTimeout(int timeout, TimeUnit timeUnit) {
        super.setTimeout(timeout, timeUnit);

        return this;
    }

    /** {@inheritDoc} */
    @Override public SqlFieldsQueryEx setCollocated(boolean collocated) {
        super.setCollocated(collocated);

        return this;
    }

    /** {@inheritDoc} */
    @Override public SqlFieldsQueryEx setEnforceJoinOrder(boolean enforceJoinOrder) {
        super.setEnforceJoinOrder(enforceJoinOrder);

        return this;
    }

    /** {@inheritDoc} */
    @Override public SqlFieldsQueryEx setDistributedJoins(boolean distributedJoins) {
        super.setDistributedJoins(distributedJoins);

        return this;
    }

    /** {@inheritDoc} */
    @Override public SqlFieldsQueryEx setPageSize(int pageSize) {
        super.setPageSize(pageSize);

        return this;
    }

    /** {@inheritDoc} */
    @Override public SqlFieldsQueryEx setLocal(boolean loc) {
        super.setLocal(loc);

        return this;
    }

    /**
     * @return Nested transactions handling mode - behavior when the user attempts to open a transaction in scope of
     * another transaction.
     */
    public NestedTxMode getNestedTxMode() {
        return nestedTxMode;
    }

    /**
     * @param nestedTxMode Nested transactions handling mode - behavior when the user attempts to open a transaction
     * in scope of another transaction.
     */
    public void setNestedTxMode(NestedTxMode nestedTxMode) {
        this.nestedTxMode = nestedTxMode;
    }

    /**
     * @return Auto commit flag.
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /**
     * @param autoCommit Auto commit flag.
     */
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    /** {@inheritDoc} */
    @Override public SqlFieldsQuery copy() {
        return new SqlFieldsQueryEx(this);
    }

    /**
     * Adds batched arguments.
     *
     * @param args Batched arguments.
     */
    public void addBatchedArgs(Object[] args) {
        if (this.batchedArgs == null)
            this.batchedArgs = new ArrayList<>();

        this.batchedArgs.add(args);
    }

    /**
     * Clears batched arguments.
     */
    public void clearBatchedArgs() {
        this.batchedArgs = null;
    }

    /**
     * Returns batched arguments.
     *
     * @return Batched arguments.
     */
    public List<Object[]> batchedArguments() {
        return this.batchedArgs;
    }

    /**
     * Checks if query is batched.
     *
     * @return {@code True} if batched.
     */
    public boolean isBatched() {
        return !F.isEmpty(batchedArgs);
    }

    /**
     * Return memory limit for query.
     *
     * Note: Zero value means a default value is used.
     *
     * Note: Every query (Map\Reduce) will have own limit and track memory independently.
     * Query can have few Map queries (e.g. an additional Map query per sub-select).
     * With QueryParallelism query can allocate MaxMemory*QueryParallelismLevel.
     *
     * @return Memory size in bytes.
     */
    public long getMaxMemory() {
        return maxMem;
    }

    /**
     * Sets memory limit for query.
     *
     * Note: Zero value means a default value is used.
     *
     * Note: Every query (Map\Reduce) will have own limit and track memory independently.
     * Query can have few Map queries (e.g. an additional Map query per sub-select).
     * With QueryParallelism query can allocate MaxMemory*QueryParallelismLevel.
     *
     * @param maxMem Memory size in bytes.
     * @return {@code this} for chaining.
     */
    public SqlFieldsQuery setMaxMemory(long maxMem) {
        this.maxMem = maxMem;

        return this;
    }
}
