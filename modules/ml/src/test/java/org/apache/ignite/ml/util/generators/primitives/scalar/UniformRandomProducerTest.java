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

package org.apache.ignite.ml.util.generators.primitives.scalar;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link UniformRandomProducer}.
 */
public class UniformRandomProducerTest {
    /** */
    @Test
    public void testGet() {
        Random random = new Random(0L);
        double[] bounds = Arrays.asList(random.nextInt(10) - 5, random.nextInt(10) - 5)
            .stream().sorted().mapToDouble(x -> x)
            .toArray();

        double min = Math.min(bounds[0], bounds[1]);
        double max = Math.max(bounds[0], bounds[1]);

        double mean = (min + max) / 2;
        double variance = Math.pow(min - max, 2) / 12;
        UniformRandomProducer producer = new UniformRandomProducer(min, max, 0L);

        final int N = 500000;
        double meanStat = IntStream.range(0, N).mapToDouble(i -> producer.get()).sum() / N;
        double varianceStat = IntStream.range(0, N).mapToDouble(i -> Math.pow(producer.get() - mean, 2)).sum() / N;

        assertEquals(mean, meanStat, 0.01);
        assertEquals(variance, varianceStat, 0.1);
    }

    /** */
    @Test
    public void testSeedConsidering() {
        UniformRandomProducer producer1 = new UniformRandomProducer(0, 1, 0L);
        UniformRandomProducer producer2 = new UniformRandomProducer(0, 1, 0L);

        assertEquals(producer1.get(), producer2.get(), 0.0001);
    }

    /** */
    @Test(expected = IllegalArgumentException.class)
    public void testFail() {
        new UniformRandomProducer(1, 0, 0L);
    }
}
