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

package org.apache.ignite.internal.commandline.shutdown;

import org.apache.ignite.ShutdownPolicy;

/**
 * Argumants of shutdown policy command.
 */
public class ShutdownPolicyArgument {
    /**
     * Policy to set, or {@code null} if need only display a state.
     */
    private ShutdownPolicy shutdown;

    /**
     * @param shutdown Shutdown policy.
     */
    public ShutdownPolicyArgument(ShutdownPolicy shutdown) {
        this.shutdown = shutdown;
    }

    /**
     * @return Shutdown policy.
     */
    public ShutdownPolicy getShutdown() {
        return shutdown;
    }

    /**
     * Builder of {@link ShutdownPolicyArgument}.
     */
    public static class Builder {
        /**
         * Policy to set, or {@code null} if need only display a state.
         */
        private ShutdownPolicy shutdown;

        /** Default constructor. */
        public Builder() {
        }

        /**
         * @param shutdown Shutdown policy.
         */
        public void setShutdownPolicy(ShutdownPolicy shutdown) {
            this.shutdown = shutdown;
        }

        /**
         * @return {@link ShutdownPolicyArgument}.
         */
        public ShutdownPolicyArgument build() {
            return new ShutdownPolicyArgument(shutdown);
        }
    }
}
