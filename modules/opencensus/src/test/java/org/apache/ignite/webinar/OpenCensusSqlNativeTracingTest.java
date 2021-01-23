/*
 * Copyright 2020 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.webinar;

import java.util.List;

import io.opencensus.exporter.trace.zipkin.ZipkinExporterConfiguration;
import io.opencensus.exporter.trace.zipkin.ZipkinTraceExporter;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.TestRecordingCommunicationSpi;
import org.apache.ignite.internal.processors.monitoring.opencensus.AbstractTracingTest;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.spi.tracing.TracingConfigurationCoordinates;
import org.apache.ignite.spi.tracing.TracingConfigurationParameters;
import org.apache.ignite.spi.tracing.TracingSpi;
import org.apache.ignite.spi.tracing.opencensus.OpenCensusTracingSpi;
import org.apache.ignite.testframework.GridTestUtils;
import org.junit.Test;

import static org.apache.ignite.spi.tracing.Scope.CACHE_API_READ;
import static org.apache.ignite.spi.tracing.Scope.COMMUNICATION;
import static org.apache.ignite.spi.tracing.Scope.SQL;
import static org.apache.ignite.spi.tracing.TracingConfigurationParameters.SAMPLING_RATE_ALWAYS;

/**
 * Tests tracing of SQL queries based on {@link OpenCensusTracingSpi}.
 */
public class OpenCensusSqlNativeTracingTest extends AbstractTracingTest {
    /** Key count. */
    private static final int KEY_CNT = 10_000;

    /** Cli. */
    private IgniteEx cli;

    /** */
    @Override protected void beforeTestsStarted() throws Exception {
        ZipkinTraceExporter.createAndRegister(
            ZipkinExporterConfiguration.builder().setV2Url("http://localhost:9411/api/v2/spans")
                .setServiceName("ignite-cluster").build());
    }

    /** {@inheritDoc} */
    @Override protected TracingSpi<?> getTracingSpi() {
        return new OpenCensusTracingSpi();
    }

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        return super.getConfiguration(igniteInstanceName)
            .setCommunicationSpi(new TestRecordingCommunicationSpi())
            .setCacheConfiguration(
                new CacheConfiguration()
                    .setName("*")
                    .setSqlFunctionClasses(GridTestUtils.SqlTestFunctions.class)
            );
    }

    @Override protected long getTestTimeout() {
        return Long.MAX_VALUE;
    }

    /** {@inheritDoc} */
    @Override public void before() throws Exception {
        super.before();

        cli = startClientGrid(GRID_CNT);

        cli.tracingConfiguration().set(
            new TracingConfigurationCoordinates.Builder(SQL).build(),
            new TracingConfigurationParameters.Builder()
                .withSamplingRate(SAMPLING_RATE_ALWAYS).build());

        cli.tracingConfiguration().set(
            new TracingConfigurationCoordinates.Builder(COMMUNICATION).build(),
            new TracingConfigurationParameters.Builder()
                .withSamplingRate(SAMPLING_RATE_ALWAYS).build());

        init();
    }

    /** */
    private void init() {
        sql("CREATE TABLE TEST (id INT PRIMARY KEY, val0 VARCHAR, val1 INT) WITH\"CACHE_NAME='TEST'\"");
        sql("CREATE INDEX idx0 on TEST(val1)");

        for (int i = 0; i < KEY_CNT; ++i)
            sql("INSERT INTO TEST (id, val0, val1) VALUES (?, ?, ?)", i, "val0_" + i, i);

        for (int i = 0; i < 100; ++i)
            sql("SELECT ID FROM TEST WHERE val1 = " + i).getAll().size();
    }

    /** */
    @Test
    public void test() throws Exception {
        sql("SELECT ID FROM TEST WHERE val0 > ?", "val0_0").getAll();

        for (int i = 0; i < 100; ++i) {
            System.out.println(sql("SELECT ID FROM TEST WHERE val1 = " + i).getAll().size());
            sql("UPDATE TEST SET val1 = ? WHERE id = ?", i, i);

            U.sleep(10);
        }

        System.out.println("+++ done");

        U.sleep(500000000);
    }

    /** */
    @Test
    public void testLong() throws Exception {
        while (true) {
            try {
                System.out.println("+++ start");

                sql("SELECT ID, delay(1000) FROM TEST WHERE val0 > ?", "val0_0").getAll();

                System.out.println("+++ done");

                U.sleep(500000000);
            }
            catch (Throwable e) {
                // No-op.
            }
        }
    }

    /** */
    protected FieldsQueryCursor<List<?>> sql( String sql, Object... args) {
        SqlFieldsQuery qry = new SqlFieldsQuery(sql)
            .setArgs(args);

        return cli.context().query().querySqlFields(qry, false);
    }
}
