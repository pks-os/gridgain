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

package org.apache.ignite.examples.streaming.wordcount;

import javax.cache.configuration.FactoryBuilder;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import org.apache.ignite.cache.affinity.AffinityUuid;
import org.apache.ignite.configuration.CacheConfiguration;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Configuration for the streaming cache to store the stream of words.
 * This cache is configured with sliding window of 1 second, which means that
 * data older than 1 second will be automatically removed from the cache.
 */
public class CacheConfig {
    /**
     * Configure streaming cache.
     */
    public static CacheConfiguration<AffinityUuid, String> wordCache() {
        CacheConfiguration<AffinityUuid, String> cfg = new CacheConfiguration<>("words");

        // Index all words streamed into cache.
        cfg.setIndexedTypes(AffinityUuid.class, String.class);

        // Sliding window of 1 seconds.
        cfg.setExpiryPolicyFactory(FactoryBuilder.factoryOf(new CreatedExpiryPolicy(new Duration(SECONDS, 1))));

        return cfg;
    }
}