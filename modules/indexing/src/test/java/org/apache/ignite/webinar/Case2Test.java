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
import java.util.UUID;

import org.apache.ignite.Ignite;
import org.apache.ignite.binary.BinaryObjectBuilder;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.failure.StopNodeFailureHandler;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.processors.cache.index.AbstractIndexingCommonTest;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.internal.util.typedef.G;
import org.junit.Test;

/**
 * Tests for local query execution in lazy mode.
 */
public class Case2Test extends AbstractIndexingCommonTest {
    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        return super.getConfiguration(igniteInstanceName)
            .setFailureHandler(new StopNodeFailureHandler());
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
        awaitPartitionMapExchange(true, true, null);
        sql("CREATE TABLE TEST (id0 INT, id1 BIGINT, name VARCHAR, price INT," +
            " PRIMARY KEY (id0, id1)) ");

        sql("CREATE INDEX idx0 on TEST(price)");
//        sql("CREATE INDEX idx0 on TEST(price) INLINE_SIZE 12");

        for (int i = 0; i < 100; ++i )
            sql("INSERT INTO TEST VALUES (?, ?, ?, ?, ?, ?)", i, (long)i, "val_" + i, i, i, UUID.randomUUID());

//        sql("SELECT * FROM TEST WHERE price > 10")
//            .getAll().forEach(r -> System.out.println("+++ " + r));
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
