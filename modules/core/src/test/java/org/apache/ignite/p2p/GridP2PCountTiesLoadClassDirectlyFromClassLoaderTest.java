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

package org.apache.ignite.p2p;

import java.net.URL;
import java.net.URLClassLoader;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.DeploymentMode;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.testframework.config.GridTestProperties;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.apache.ignite.testframework.junits.common.GridCommonTest;
import org.junit.Test;

/**
 * Checks count of tries to load class directly from class loader.
 */
@GridCommonTest(group = "P2P")
public class GridP2PCountTiesLoadClassDirectlyFromClassLoaderTest extends GridCommonAbstractTest {
    /** P2P class path property. */
    public static final String CLS_PATH_PROPERTY = "p2p.uri.cls";
    /** Compute task name. */
    private static String COMPUTE_TASK_NAME = "org.apache.ignite.tests.p2p.compute.ExternalCallable";

    /** Deployment mode. */
    private DeploymentMode depMode;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        return super.getConfiguration(igniteInstanceName)
            .setDeploymentMode(depMode)
            .setPeerClassLoadingEnabled(true);
    }

    /**
     * @throws Exception if error occur.
     */
    public void executeP2PTask(DeploymentMode depMode) throws Exception {
        try {
            CountTriesClassLoader testCountLdr = new CountTriesClassLoader(Thread.currentThread()
                .getContextClassLoader(), log);

            this.depMode = depMode;

            Thread.currentThread().setContextClassLoader(testCountLdr);

            String path = GridTestProperties.getProperty(CLS_PATH_PROPERTY);

            ClassLoader urlClsLdr = new URLClassLoader(new URL[] {new URL(path)}, testCountLdr);

            Ignite ignite = startGrids(2);

            ignite.compute(ignite.cluster().forRemotes()).call((IgniteCallable)urlClsLdr.loadClass(COMPUTE_TASK_NAME)
                .newInstance());

            int count = testCountLdr.count;

            ignite.compute(ignite.cluster().forRemotes()).call((IgniteCallable)urlClsLdr.loadClass(COMPUTE_TASK_NAME)
                .newInstance());
            ignite.compute(ignite.cluster().forRemotes()).call((IgniteCallable)urlClsLdr.loadClass(COMPUTE_TASK_NAME)
                .newInstance());
            ignite.compute(ignite.cluster().forRemotes()).call((IgniteCallable)urlClsLdr.loadClass(COMPUTE_TASK_NAME)
                .newInstance());

            assertEquals(count, testCountLdr.count);
        }
        finally {
            stopAllGrids();
        }
    }

    /**
     * @throws Exception if error occur.
     */
    @Test
    public void testPrivateMode() throws Exception {
        executeP2PTask(DeploymentMode.PRIVATE);
    }

    /**
     * @throws Exception if error occur.
     */
    @Test
    public void testIsolatedMode() throws Exception {
        executeP2PTask(DeploymentMode.ISOLATED);
    }

    /**
     * @throws Exception if error occur.
     */
    @Test
    public void testContinuousMode() throws Exception {
        executeP2PTask(DeploymentMode.CONTINUOUS);
    }

    /**
     * @throws Exception if error occur.
     */
    @Test
    public void testSharedMode() throws Exception {
        executeP2PTask(DeploymentMode.SHARED);
    }

    /**
     * Test count class loader.
     */
    private static class CountTriesClassLoader extends ClassLoader {
        /** Count of tries. */
        int count = 0;

        /** Ignite logger. */
        IgniteLogger log;

        /**
         * @param parent Parent class loader.
         */
        public CountTriesClassLoader(ClassLoader parent, IgniteLogger log) {
            super(parent);

            this.log = log;
        }

        /** {@inheritDoc} */
        @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (COMPUTE_TASK_NAME.equals(name))
                U.dumpStack(log, "Try to load class: " + name + " " + ++count);

            return super.loadClass(name, resolve);
        }
    }
}
