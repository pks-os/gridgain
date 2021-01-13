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

package org.apache.ignite.webinar;

import java.util.List;

import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.failure.StopNodeFailureHandler;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.processors.cache.index.AbstractIndexingCommonTest;
import org.junit.Test;

/**
 * Tests for local query execution in lazy mode.
 */
public class ExplainTest extends AbstractIndexingCommonTest {
    /** Keys count. */
    private static final int KEY_CNT = 100;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        return super.getConfiguration(igniteInstanceName)
            .setFailureHandler(new StopNodeFailureHandler());
    }
    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        super.beforeTest();

        startGrid();
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        stopAllGrids();

        super.afterTest();
    }

    /** */
    @Test
    public void testExplainPlain() throws Exception {
        awaitPartitionMapExchange(true, true, null);

        sql("CREATE TABLE TEST (id INT PRIMARY KEY, val0 VARCHAR, val1 INT)");
        sql("CREATE INDEX idx0 on TEST(val1)");

        for (int i = 0; i < KEY_CNT; ++i)
           sql("INSERT INTO TEST (id, val0, val1) VALUES (?, ?, ?)", i, "val0_" + i, i);

        System.out.println("+++ " + sql("EXPLAIN SELECT ID FROM TEST WHERE val0 = ? AND val1 < ?", 1, "val").getAll());
    }

    /** */
    @Test
    public void testExplainComplex() throws Exception {
        awaitPartitionMapExchange(true, true, null);

        sql("CREATE TABLE TEST0 (id INT PRIMARY KEY, grpId INT,  val0 VARCHAR, val1 INT)");
        sql("CREATE TABLE TEST1 (id INT PRIMARY KEY, val0 VARCHAR, val1 INT)");
        sql("CREATE INDEX idx0 on TEST0(val1)");
        sql("CREATE INDEX idx1 on TEST0(val0)");

        for (int i = 0; i < KEY_CNT; ++i)
            sql("INSERT INTO TEST0 (id, grpId, val0, val1) VALUES (?, ?, ?, ?)", i, i / 10, "val0_" + i, i);

        System.out.println("+++ " + sql("EXPLAIN " +
            "SELECT * FROM " +
                "(SELECT grpId, val0 FROM TEST0 WHERE val0 > ? GROUP BY grpId HAVING (AVG(val1)) < ?) AS AGG_RES " +
                "JOIN TEST1 ON TEST1.id = AGG_RES.grpId",
            1, 2).getAll());
    }

    /**
     * @param sql SQL query.
     * @param args Query parameters.
     * @return Results cursor.
     */
    private FieldsQueryCursor<List<?>> sql(String sql, Object... args) {
        return sql(grid(), sql, args);
    }

    /**
     * @param ign Node.
     * @param sql SQL query.
     * @param args Query parameters.
     * @return Results cursor.
     */
    private FieldsQueryCursor<List<?>> sql(IgniteEx ign, String sql, Object... args) {
        return ign.context().query().querySqlFields(new SqlFieldsQuery(sql)
            .setLazy(true)
            .setArgs(args), false);
    }
}
