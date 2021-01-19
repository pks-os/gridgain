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
import java.util.concurrent.atomic.AtomicInteger;

import io.opencensus.exporter.trace.zipkin.ZipkinExporterConfiguration;
import io.opencensus.exporter.trace.zipkin.ZipkinTraceExporter;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.TestRecordingCommunicationSpi;
import org.apache.ignite.internal.processors.monitoring.opencensus.AbstractTracingTest;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.spi.tracing.TracingConfigurationCoordinates;
import org.apache.ignite.spi.tracing.TracingConfigurationParameters;
import org.apache.ignite.spi.tracing.TracingSpi;
import org.apache.ignite.spi.tracing.opencensus.OpenCensusTracingSpi;
import org.junit.Test;

import static java.lang.Integer.parseInt;
import static org.apache.ignite.spi.tracing.Scope.SQL;
import static org.apache.ignite.spi.tracing.TracingConfigurationParameters.SAMPLING_RATE_ALWAYS;
import static org.apache.ignite.testframework.GridTestUtils.runAsync;

/**
 * Tests tracing of SQL queries based on {@link OpenCensusTracingSpi}.
 */
public class OpenCensusSqlNativeTracingTest extends AbstractTracingTest {
    /** Page size for queries. */
    protected static final int PAGE_SIZE = 20;

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
            .setCommunicationSpi(new TestRecordingCommunicationSpi());
    }

    /** {@inheritDoc} */
    @Override public void before() throws Exception {
        super.before();

        cli = startClientGrid(GRID_CNT);

        cli.tracingConfiguration().set(
            new TracingConfigurationCoordinates.Builder(SQL).build(),
            new TracingConfigurationParameters.Builder()
                .withSamplingRate(SAMPLING_RATE_ALWAYS).build());

        init();
    }

    /** */
    private void init() {
        sql("CREATE TABLE TEST (id INT PRIMARY KEY, val0 VARCHAR, val1 INT)");
        sql("CREATE INDEX idx0 on TEST(val1)");

        for (int i = 0; i < KEY_CNT; ++i)
            sql("INSERT INTO TEST (id, val0, val1) VALUES (?, ?, ?)", i, "val0_" + i, i);
    }

    /** */
    @Test
    public void test() throws Exception {
        for (int i = 0; i < 100; ++i) {
            System.out.println(sql("SELECT ID FROM TEST WHERE val1 = " + i).getAll().size());
//            sql("UPDATE TEST SET val1 = ? WHERE id = ?", i, i);

            U.sleep(100);
        }

        sql("SELECT ID FROM TEST WHERE val0 > ?", "val0_0").getAll();

        System.out.println("+++ done");

        U.sleep(500000000);
    }


    /** */
    protected FieldsQueryCursor<List<?>> sql( String sql, Object... args) {
        SqlFieldsQuery qry = new SqlFieldsQuery(sql)
            .setLazy(true)
            .setArgs(args);

        return cli.context().query().querySqlFields(qry, false);
    }
}
