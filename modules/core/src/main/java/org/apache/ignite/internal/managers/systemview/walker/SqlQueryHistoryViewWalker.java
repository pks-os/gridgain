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

package org.apache.ignite.internal.managers.systemview.walker;

import java.util.Date;
import org.apache.ignite.spi.systemview.view.SqlQueryHistoryView;
import org.apache.ignite.spi.systemview.view.SystemViewRowAttributeWalker;

/**
 * Generated by {@code org.apache.ignite.codegen.SystemViewRowAttributeWalkerGenerator}.
 * {@link SqlQueryHistoryView} attributes walker.
 * 
 * @see SqlQueryHistoryView
 */
public class SqlQueryHistoryViewWalker implements SystemViewRowAttributeWalker<SqlQueryHistoryView> {
    /** {@inheritDoc} */
    @Override public void visitAll(AttributeVisitor v) {
        v.accept(0, "schemaName", String.class);
        v.accept(1, "sql", String.class);
        v.accept(2, "local", boolean.class);
        v.accept(3, "executions", long.class);
        v.accept(4, "failures", long.class);
        v.accept(5, "durationMin", long.class);
        v.accept(6, "durationMax", long.class);
        v.accept(7, "lastStartTime", Date.class);
        v.accept(8, "diskAllocationMax", long.class);
        v.accept(9, "diskAllocationMin", long.class);
        v.accept(10, "diskAllocationTotalMax", long.class);
        v.accept(11, "diskAllocationTotalMin", long.class);
        v.accept(12, "memoryMax", long.class);
        v.accept(13, "memoryMin", long.class);
    }

    /** {@inheritDoc} */
    @Override public void visitAll(SqlQueryHistoryView row, AttributeWithValueVisitor v) {
        v.accept(0, "schemaName", String.class, row.schemaName());
        v.accept(1, "sql", String.class, row.sql());
        v.acceptBoolean(2, "local", row.local());
        v.acceptLong(3, "executions", row.executions());
        v.acceptLong(4, "failures", row.failures());
        v.acceptLong(5, "durationMin", row.durationMin());
        v.acceptLong(6, "durationMax", row.durationMax());
        v.accept(7, "lastStartTime", Date.class, row.lastStartTime());
        v.acceptLong(8, "diskAllocationMax", row.diskAllocationMax());
        v.acceptLong(9, "diskAllocationMin", row.diskAllocationMin());
        v.acceptLong(10, "diskAllocationTotalMax", row.diskAllocationTotalMax());
        v.acceptLong(11, "diskAllocationTotalMin", row.diskAllocationTotalMin());
        v.acceptLong(12, "memoryMax", row.memoryMax());
        v.acceptLong(13, "memoryMin", row.memoryMin());
    }

    /** {@inheritDoc} */
    @Override public int count() {
        return 14;
    }
}
