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

import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

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
public class LogTest extends AbstractIndexingCommonTest {
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
    public void testHugeResult() throws Exception {
        sql("CREATE TABLE TEST (id0 INT, id1 BIGINT, name VARCHAR, price INT," +
            " PRIMARY KEY (id0, id1)) ");

        for (int i = 0; i < 10000; ++i )
            sql("INSERT INTO TEST VALUES (?, ?, ?, ?)", i, i, "val_" + i , i);

        Iterator it = sql("SELECT T0.name FROM TEST T0, TEST T1 WHERE T0.price > 10").iterator();

        while (it.hasNext())
            it.next();
    }

    /** */
    @Test
    public void testInlineSize() throws Exception {
        sql("CREATE TABLE TEST (id0 INT, id1 BIGINT, name VARCHAR(80), price INT, ts TIMESTAMP, type SMALLINT, " +
            " PRIMARY KEY (id0, id1)) ");

        sql("CREATE INDEX IDX0 ON TEST(type, price, ts, name) INLINE_SIZE 10");
//        sql("CREATE INDEX IDX0 ON TEST(type, price, ts, name)");

        for (int i = 0; i < 1000; ++i)
            sql("INSERT INTO TEST VALUES(?, ? ,? ,? ,? ,?)", i, i, "asdasdasdasasdasdfffffffffffffsdfsdfsdfsdfsdasdasdd", 0, new Date(), 0);
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
