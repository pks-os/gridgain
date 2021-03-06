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

package org.apache.ignite.internal.processors.rest.request;

import org.apache.ignite.cluster.ClusterState;

/**
 *
 */
public class GridRestChangeStateRequest extends GridRestRequest {
    /** Active. */
    private boolean active;

    /** Request current state. */
    private boolean reqCurrentState;

    /** If {@code true}, cluster deactivation will be forced. */
    private boolean forceDeactivation;

    /**
     *
     */
    public boolean active() {
        return active;
    }

    /**
     *
     */
    public void active(boolean active) {
        this.active = active;
    }

    /**
     *
     */
    public boolean isReqCurrentState() {
        return reqCurrentState;
    }

    /**
     *
     */
    public void reqCurrentState() {
        reqCurrentState = true;
    }

    /**
     * @param forceDeactivation If {@code true}, cluster deactivation will be forced.
     */
    public void forceDeactivation(boolean forceDeactivation) {
        this.forceDeactivation = forceDeactivation;
    }

    /**
     * @return {@code True} if cluster deactivation will be forced. {@code False} otherwise.
     * @see ClusterState#INACTIVE
     */
    public boolean forceDeactivation() {
        return forceDeactivation;
    }
}
