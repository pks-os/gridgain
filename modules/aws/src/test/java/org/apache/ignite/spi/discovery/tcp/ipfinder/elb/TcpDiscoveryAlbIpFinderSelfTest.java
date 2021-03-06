/*
 * Copyright 2020 GridGain Systems, Inc. and Contributors.
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

package org.apache.ignite.spi.discovery.tcp.ipfinder.elb;

import org.apache.ignite.spi.IgniteSpiException;
import org.apache.ignite.spi.discovery.tcp.ipfinder.TcpDiscoveryIpFinderAbstractSelfTest;

/**
 * TcpDiscoveryAlbIpFinderSelfTest test.
 */
public class TcpDiscoveryAlbIpFinderSelfTest extends TcpDiscoveryIpFinderAbstractSelfTest<TcpDiscoveryAlbIpFinder> {
    /**
     * Constructor.
     *
     * @throws Exception If any error occurs.
     */
    public TcpDiscoveryAlbIpFinderSelfTest() throws Exception {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override protected TcpDiscoveryAlbIpFinder ipFinder() throws Exception {
        return null;
    }

    /** {@inheritDoc} */
    @Override public void testIpFinder() throws Exception {
        TcpDiscoveryAlbIpFinder ipFinder = new TcpDiscoveryAlbIpFinder();

        ipFinder.setRegion(null);

        try {
            ipFinder.getRegisteredAddresses();
        }
        catch (IgniteSpiException e) {
            assertTrue(e.getMessage().startsWith("One or more configuration parameters are invalid"));
        }

        ipFinder = new TcpDiscoveryAlbIpFinder();

        ipFinder.setTargetGrpARN(null);

        try {
            ipFinder.getRegisteredAddresses();
        }
        catch (IgniteSpiException e) {
            assertTrue(e.getMessage().startsWith("One or more configuration parameters are invalid"));
        }

        ipFinder = new TcpDiscoveryAlbIpFinder();

        ipFinder.setCredentialsProvider(null);

        try {
            ipFinder.getRegisteredAddresses();
        }
        catch (IgniteSpiException e) {
            assertTrue(e.getMessage().startsWith("One or more configuration parameters are invalid"));
        }
    }
}
