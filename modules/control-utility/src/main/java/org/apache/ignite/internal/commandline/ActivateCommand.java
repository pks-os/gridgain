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


package org.apache.ignite.internal.commandline;

import java.util.logging.Logger;
import org.apache.ignite.internal.client.GridClient;
import org.apache.ignite.internal.client.GridClientClusterState;
import org.apache.ignite.internal.client.GridClientConfiguration;
import org.apache.ignite.internal.client.GridClientException;

import static org.apache.ignite.internal.commandline.CommandList.ACTIVATE;
import static org.apache.ignite.internal.commandline.CommandList.SET_STATE;

/**
 * Activate cluster command.
 * @deprecated Use {@link ClusterStateChangeCommand} instead.
 */
@Deprecated
public class ActivateCommand extends AbstractCommand<Void> {
    /** {@inheritDoc} */
    @Override public void printUsage(Logger logger) {
        Command.usage(logger, "Activate cluster (deprecated. Use " + SET_STATE.toString() + " instead):", ACTIVATE);
    }

    /**
     * Activate cluster.
     *
     * @param cfg Client configuration.
     * @throws GridClientException If failed to activate.
     */
    @Override public Object execute(GridClientConfiguration cfg, Logger logger) throws Exception {
        logger.warning("Command deprecated. Use " + SET_STATE.toString() + " instead.");

        try (GridClient client = Command.startClient(cfg)) {
            GridClientClusterState state = client.state();

            state.active(true);

            logger.info("Cluster activated");
        }
        catch (Throwable e) {
            logger.severe("Failed to activate cluster.");

            throw e;
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override public Void arg() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public String name() {
        return ACTIVATE.toCommandName();
    }
}
