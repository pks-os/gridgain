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

package org.apache.ignite.internal.processors.query;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.cache.CacheException;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.processors.cache.index.AbstractIndexingCommonTest;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.testframework.GridTestUtils;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

/**
 * Tests for illegal SQL schemas in node and cache configurations.
 */
@SuppressWarnings({"ThrowableNotThrown", "unchecked"})
public class SqlIllegalSchemaSelfTest extends AbstractIndexingCommonTest {
    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        stopAllGrids();
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testBadCacheName() throws Exception {
        IgniteConfiguration cfg = getConfiguration();

        final String invalidCache = QueryUtils.sysSchemaName();

        cfg.setCacheConfiguration(new CacheConfiguration().setName(invalidCache));

        GridTestUtils.assertThrows(log, new Callable<Void>() {
            @Override public Void call() throws Exception {
                Ignition.start(cfg);

                return null;
            }
        }, IgniteException.class, "SQL schema name derived from cache name is reserved (please set explicit SQL " +
            "schema name through CacheConfiguration.setSqlSchema() or choose another cache name) [cacheName="
            + invalidCache + ", " + "schemaName=null]");
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testBadCacheNameDynamic() throws Exception {
        final String invalidCache = QueryUtils.sysSchemaName();

        doubleConsumerAccept(
            (node) -> {
                try {
                    node.getOrCreateCache(new CacheConfiguration().setName(invalidCache));
                }
                catch (CacheException e) {
                    assertTrue(hasCause(e, IgniteCheckedException.class,
                        "SQL schema name derived from cache name is reserved (please set explicit SQL " +
                            "schema name through CacheConfiguration.setSqlSchema() or choose another cache name) [" +
                            "cacheName=" + invalidCache + ", schemaName=null]"));

                    return;
                }
                catch (Throwable e) {
                    fail("Exception class is not as expected [expected=" +
                        CacheException.class + ", actual=" + e.getClass() + ']');
                }

                fail("Exception has not been thrown.");
            }
        );
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testBadSchemaLower() throws Exception {
        IgniteConfiguration cfg = getConfiguration();

        final String invalidSchema = QueryUtils.sysSchemaName().toLowerCase();

        cfg.setCacheConfiguration(new CacheConfiguration().setName("CACHE").setSqlSchema(invalidSchema));

        GridTestUtils.assertThrows(log, new Callable<Void>() {
            @Override public Void call() throws Exception {
                Ignition.start(cfg);

                return null;
            }
        }, IgniteException.class, "SQL schema name is reserved (please choose another one) [cacheName=CACHE, " +
            "schemaName=" + invalidSchema + "]");
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testBadSchemaLowerDynamic() throws Exception {
        final String invalidSchema = QueryUtils.sysSchemaName().toLowerCase();

        doubleConsumerAccept(
            (node) -> {
                try {
                    node.getOrCreateCache(
                        new CacheConfiguration().setName("CACHE").setSqlSchema(invalidSchema)
                    );
                }
                catch (CacheException e) {
                    assertTrue(hasCause(e, IgniteCheckedException.class,
                        "SQL schema name is reserved (please choose another one) [cacheName=CACHE, schemaName="
                            + invalidSchema + "]"));

                    return;
                }
                catch (Throwable e) {
                    fail("Exception class is not as expected [expected=" +
                        CacheException.class + ", actual=" + e.getClass() + ']');
                }

                fail("Exception has not been thrown.");
            }
        );
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testBadSchemaUpper() throws Exception {
        IgniteConfiguration cfg = getConfiguration();

        final String invalidSchema = QueryUtils.sysSchemaName().toUpperCase();

        cfg.setCacheConfiguration(new CacheConfiguration().setName("CACHE")
            .setSqlSchema(invalidSchema));

        GridTestUtils.assertThrows(log, new Callable<Void>() {
            @Override public Void call() throws Exception {
                Ignition.start(cfg);

                return null;
            }
        }, IgniteException.class, "SQL schema name is reserved (please choose another one) [cacheName=CACHE, " +
            "schemaName=" + invalidSchema + "]");
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testBadSchemaUpperDynamic() throws Exception {
        final String invalidSchema = QueryUtils.sysSchemaName().toUpperCase();

        doubleConsumerAccept(
            (node) -> {
                try {
                    node.getOrCreateCache(
                        new CacheConfiguration().setName("CACHE").setSqlSchema(invalidSchema)
                    );
                }
                catch (CacheException e) {
                    assertTrue(hasCause(e, IgniteCheckedException.class,
                        "SQL schema name is reserved (please choose another one) [cacheName=CACHE, schemaName="
                            + invalidSchema + "]"));

                    return;
                }
                catch (Throwable e) {
                    fail("Exception class is not as expected [expected=" +
                        CacheException.class + ", actual=" + e.getClass() + ']');
                }

                fail("Exception has not been thrown.");
            }
        );
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testBadSchemaQuoted() throws Exception {
        IgniteConfiguration cfg = getConfiguration();

        final String invalidSchema = QueryUtils.sysSchemaName().toUpperCase();

        cfg.setCacheConfiguration(new CacheConfiguration().setName("CACHE")
            .setSqlSchema("\"" + invalidSchema + "\""));

        GridTestUtils.assertThrows(log, new Callable<Void>() {
            @Override public Void call() throws Exception {
                Ignition.start(cfg);

                return null;
            }
        }, IgniteException.class, "SQL schema name is reserved (please choose another one) [cacheName=CACHE, " +
            "schemaName=\"" + invalidSchema + "\"]");
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testBadSchemaQuotedDynamic() throws Exception {
        final String invalidSchema = QueryUtils.sysSchemaName().toUpperCase();

        doubleConsumerAccept(
            (node) -> {
                try {
                    node.getOrCreateCache(
                        new CacheConfiguration().setName("CACHE")
                            .setSqlSchema("\"" + invalidSchema + "\"")
                    );
                }
                catch (CacheException e) {
                    assertTrue(hasCause(e, IgniteCheckedException.class,
                        "SQL schema name is reserved (please choose another one) [cacheName=CACHE, schemaName=\""
                            + invalidSchema + "\"]"));

                    return;
                }
                catch (Throwable e) {
                    fail("Exception class is not as expected [expected=" +
                        CacheException.class + ", actual=" + e.getClass() + ']');
                }

                fail("Exception has not been thrown.");
            }
        );
    }

    /**
     * Executes double call of consumer's accept method with passed Ignite instance.
     *
     * @param cons Consumer.
     * @throws Exception If failed.
     */
    private void doubleConsumerAccept(Consumer<Ignite> cons) throws Exception {
        Ignite node = startGrid();

        cons.accept(node);

        cons.accept(node);
    }

    /**
     * Checks if passed in {@code 'Throwable'} has given class in {@code 'cause'} hierarchy
     * <b>including</b> that throwable itself and it contains passed message.
     * <p>
     * Note that this method follows includes {@link Throwable#getSuppressed()}
     * into check.
     *
     * @param t Throwable to check (if {@code null}, {@code false} is returned).
     * @param cls Cause class to check (if {@code null}, {@code false} is returned).
     * @param msg Message to check.
     * @return {@code True} if one of the causing exception is an instance of passed in classes
     *      and it contains the passed message, {@code false} otherwise.
     */
    private boolean hasCause(@Nullable Throwable t, Class<?> cls, String msg) {
        if (t == null)
            return false;

        assert cls != null;

        for (Throwable th = t; th != null; th = th.getCause()) {
            if (cls.isAssignableFrom(th.getClass()) && F.eq(th.getMessage(), msg))
                return true;

            for (Throwable n : th.getSuppressed()) {
                if (hasCause(n, cls, msg))
                    return true;
            }

            if (th.getCause() == th)
                break;
        }

        return false;
    }
}
