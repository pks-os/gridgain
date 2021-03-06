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

package org.apache.ignite.internal.processors.query.h2.twostep;

import org.gridgain.internal.h2.api.TableEngine;
import org.gridgain.internal.h2.command.ddl.CreateTableData;
import org.gridgain.internal.h2.table.Table;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.apache.ignite.internal.processors.query.h2.sql.GridSqlQuerySplitter.mergeTableIdentifier;

/**
 * Engine to create reduce table.
 */
public class ReduceTableEngine implements TableEngine {
    /** */
    private static final ThreadLocal<ReduceTableWrapper> CREATED_TBL = new ThreadLocal<>();

    /**
     * Create merge table over the given connection with provided index.
     *
     * @param conn Connection.
     * @param idx Index.
     * @return Created table.
     */
    public static ReduceTableWrapper create(Connection conn, int idx) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE " + mergeTableIdentifier(idx) +
                "(fake BOOL) ENGINE \"" + ReduceTableEngine.class.getName() + '"');
        }
        catch (SQLException e) {
            throw new IllegalStateException(e);
        }

        ReduceTableWrapper tbl = CREATED_TBL.get();

        assert tbl != null;

        CREATED_TBL.remove();

        return tbl;
    }

    /** {@inheritDoc} */
    @Override public Table createTable(CreateTableData d) {
        assert CREATED_TBL.get() == null;

        ReduceTableWrapper tbl = new ReduceTableWrapper(
            d.schema,
            d.id,
            d.tableName,
            d.persistIndexes,
            d.persistData
        );

        CREATED_TBL.set(tbl);

        return tbl;
    }
}
