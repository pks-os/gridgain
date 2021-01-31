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

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.failure.StopNodeFailureHandler;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.processors.cache.index.AbstractIndexingCommonTest;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.internal.util.typedef.G;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.testframework.GridTestUtils;
import org.junit.Test;

/**
 * Tests for local query execution in lazy mode.
 */
public class Case3Test extends AbstractIndexingCommonTest {
    /** {@inheritDoc} */
    @Override protected long getTestTimeout() {
        return Long.MAX_VALUE;
    }

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        return super.getConfiguration(igniteInstanceName)
            .setFailureHandler(new StopNodeFailureHandler())
            .setCacheConfiguration(
                new CacheConfiguration()
                    .setName("*")
                    .setSqlFunctionClasses(GridTestUtils.SqlTestFunctions.class)
            );
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        super.beforeTest();

        startGrids(3);
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        stopAllGrids();

        super.afterTest();
    }

    /**
     *
     */
    @Test
    public void test() throws Exception {
        // TODO: SORT WITH INDEX
        awaitPartitionMapExchange(true, true, null);
        sql("CREATE TABLE TEST(id INT PRIMARY KEY, grp_id INT, name VARCHAR, price INT)");

//        sql("CREATE INDEX idx0 on TEST(grp_id)");

        for (int i = 0; i < 100_000; ++i )
            sql("INSERT INTO TEST VALUES (?, ?, ?, ?)", i, i, "val_" + i, i);

        System.out.println("+++ start");

        GridTestUtils.runMultiThreaded(
            () -> {
                while(true) {
                    Iterator it = sql(
                        "SELECT T0.grp_id, MAX(T0.price), MIN(T0.price) " +
                            "FROM TEST T0 " +
                            "WHERE T0.price > 10 " +
                            "GROUP BY T0.grp_id")
//                    "SELECT T0.grp_id, MAX(T1.price), MIN(T1.price), delay(10) " +
//                        "FROM TEST T0, TEST T1 " +
//                        "WHERE T0.price > 10 " +
//                        "GROUP BY T0.grp_id, T1.grp_id")
                        .iterator();

                    while (it.hasNext())
                        it.next();
                }
            },
            200,
            "hi-mem-qry");
    }

    /** */
    @Test
    public void testSort() throws Exception {
        sql("CREATE TABLE TEST(id INT PRIMARY KEY, type INT, name VARCHAR, price INT, ts TIMESTAMP)");

        sql("CREATE INDEX idx_type on TEST(type)");
        sql("CREATE INDEX idx_name on TEST(name)");

        for (int i = 0; i < 100_000; ++i )
            sql("INSERT INTO TEST VALUES (?, ?, ?, ?, ?)",
                i, i % 10, "val_" + i, i, new Date(U.currentTimeMillis() + i * 1000));

        System.out.println("+++ start");

        GridTestUtils.runMultiThreaded(
            () -> {
                while(true) {
                    Iterator it = sql(
                        "SELECT * " +
                            "FROM TEST T0 USE INDEX (idx_name)" +
                            "WHERE T0.type = 0 " +
                            "ORDER BY T0.name")
                        .iterator();

                    while (it.hasNext())
                        it.next();
                }
            },
            200,
            "hi-mem-qry");
    }
    /**
     * @param sql SQL query.
     * @param args Query parameters.
     * @return Results cursor.
     */
    private FieldsQueryCursor<List<?>> sql(String sql, Object... args) {
        return sql((IgniteEx)F.first(G.allGrids()), sql, args);
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
