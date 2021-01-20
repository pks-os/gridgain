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
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.events.EventType;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.kubernetes.TcpDiscoveryKubernetesIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import java.util.Collections;
import java.util.Date;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Starts up an empty node with example compute configuration.
 */
public class ExampleNodeStartup2 {
    /**
     * Start up an empty node with example compute configuration.
     *
     * @param args Command line arguments, none required.
     * @throws IgniteException If failed.
     */
    public static void main(String[] args) throws IgniteException, InterruptedException {
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setDiscoverySpi(new TcpDiscoverySpi()
                .setIpFinder(new TcpDiscoveryMulticastIpFinder()));
                       //.setAddresses(Collections.singleton("127.0.0.1:47500"))));
        Ignite ignite = Ignition.start(cfg);

        clientConnect(ignite);


    }

    private static void clientConnect(Ignite server) throws InterruptedException {
        IgniteConfiguration ivCfg = new IgniteConfiguration();
        ivCfg.setGridName("client");

        ivCfg.setClientMode(true);
        ivCfg.setIncludeEventTypes(EventType.EVT_CLIENT_NODE_DISCONNECTED, EventType.EVT_CLIENT_NODE_RECONNECTED);
        //ivCfg.setPeerClassLoadingEnabled(true);

        TcpDiscoverySpi lvSpi = new TcpDiscoverySpi();
        TcpDiscoveryVmIpFinder lvFinder = new TcpDiscoveryMulticastIpFinder();
                //.setAddresses(Collections.singleton("127.0.0.1:47500"));

        lvSpi.setIpFinder(lvFinder);
        lvSpi.setJoinTimeout(3000);
        lvSpi.setNetworkTimeout(3000);
        lvSpi.setSocketTimeout(3000);
        ivCfg.setDiscoverySpi(lvSpi);

        // start
        Ignite ivIgniteClient = Ignition.start(ivCfg);

        ivIgniteClient.log().warning("AAAAAAA");
        System.out.println("AAAAAAA");

        ivIgniteClient.events().localListen(event -> {

                    System.out.println("BBBBBBB");
                    System.out.println(event.toString());
                    ivIgniteClient.log().warning("BBBBBBB");
                    ivIgniteClient.log().warning(event.toString());

                    System.out.println(new Date());
                    System.out.println(event);

                    return true;

                },
                EventType.EVT_CLIENT_NODE_DISCONNECTED,
                EventType.EVT_CLIENT_NODE_RECONNECTED, EventType.EVT_CLUSTER_ACTIVATED,
                EventType.EVT_CLUSTER_DEACTIVATED);

        TcpDiscoveryKubernetesIpFinder.FAIL = true;
        TcpDiscoveryMulticastIpFinder.FAIL = true;

        Ignition.stop(server.name(), true);

        Thread.sleep(100000);
    }
}
