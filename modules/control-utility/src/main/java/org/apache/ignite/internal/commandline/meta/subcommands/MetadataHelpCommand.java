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

package org.apache.ignite.internal.commandline.meta.subcommands;

import java.util.logging.Logger;
import org.apache.ignite.internal.client.GridClientConfiguration;
import org.apache.ignite.internal.commandline.AbstractCommand;
import org.apache.ignite.internal.commandline.meta.MetadataCommand;
import org.apache.ignite.internal.commandline.meta.MetadataSubCommandsList;

/** */
public class MetadataHelpCommand extends AbstractCommand<Void> {
    /** {@inheritDoc} */
    @Override public void printUsage(Logger log) {
        throw new UnsupportedOperationException("printUsage");
    }

    /** {@inheritDoc} */
    @Override public Object execute(GridClientConfiguration clientCfg, Logger log) throws Exception {
        new MetadataCommand().printUsage(log);

        return null;
    }

    /** {@inheritDoc} */
    @Override public boolean experimentalEnabled() {
        return true;
    }

    /** {@inheritDoc} */
    @Override public Void arg() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public String name() {
        return MetadataSubCommandsList.HELP.text();
    }
}
