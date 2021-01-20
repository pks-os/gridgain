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

package org.apache.ignite.testsuites;

import java.util.Arrays;
import java.util.List;
import org.apache.ignite.internal.processors.cache.InterceptorCacheConfigVariationsFullApiTest;
import org.apache.ignite.testframework.configvariations.ConfigVariationsTestSuiteBuilder;
import org.apache.ignite.testframework.junits.DynamicSuite;
import org.junit.runner.RunWith;

/**
 * Test suite for cache API.
 */
@RunWith(DynamicSuite.class)
public class InterceptorCacheConfigVariationsFullApiTestSuite {
    /** */
    public static List<Class<?>> suite() {
        List<Class<?>> classes = new ConfigVariationsTestSuiteBuilder(InterceptorCacheConfigVariationsFullApiTest.class)
            .withBasicCacheParams()
            .gridsCount(5).backups(1)
            .testedNodesCount(3).withClients()
            .classes();

        return Arrays.asList(classes.get(63), classes.get(65));
    }
}
