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

package org.apache.ignite.examples;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;

/**
 * Starts up an empty node with example compute configuration.
 */
public class ExampleNodeStartup {
    /**
     * Start up an empty node with example compute configuration.
     *
     * @param args Command line arguments, none required.
     * @throws IgniteException If failed.
     */
    public static void main(String[] args) throws IgniteException {
        Ignite ignite =Ignition.start(new IgniteConfiguration().setGridName("aaaa"));


        Ignite ignit2 = Ignition.start(new IgniteConfiguration().setGridName("asdf"));


        IgniteCache<Object, Object> myCache = ignite.createCache(new CacheConfiguration<>("myCache").setBackups(1));
        myCache.put(1, 1);
        myCache.put(2, 2);
        myCache.put(3, 3);
        myCache.put(4, 4);
        myCache.put(5, 5);

        int myCache1 = ignite.cache("myCache").localSize(CachePeekMode.PRIMARY);
        int myCache1b = ignite.cache("myCache").localSize(CachePeekMode.BACKUP);
        int myCache2 = ignit2.cache("myCache").localSize(CachePeekMode.PRIMARY);
        int myCache2b = ignit2.cache("myCache").localSize(CachePeekMode.BACKUP);
    }
}
