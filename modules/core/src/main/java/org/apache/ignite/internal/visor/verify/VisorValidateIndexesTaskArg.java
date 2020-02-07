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

package org.apache.ignite.internal.visor.verify;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;
import java.util.UUID;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.internal.visor.VisorDataTransferObject;

/**
 *
 */
public class VisorValidateIndexesTaskArg extends VisorDataTransferObject {
    /** */
    private static final long serialVersionUID = 0L;

    /** Caches. */
    private Set<String> caches;

    /** Check first K elements. */
    private int checkFirst = -1;

    /** Check through K element (skip K-1, check Kth). */
    private int checkThrough = -1;

    /** Nodes on which task will run. */
    private Set<UUID> nodes;

    /** Check that index size and cache size are same. */
    private boolean checkSizes = true;

    /**
     * Default constructor.
     */
    public VisorValidateIndexesTaskArg() {
        // No-op.
    }

    /**
     * Constructor.
     *
     * @param caches Caches.
     * @param nodes Nodes on which task will run.
     * @param checkFirst Check first K elements.
     * @param checkThrough Check through K element.
     * @param checkSizes Check that index size and cache size are same.
     */
    public VisorValidateIndexesTaskArg(
        Set<String> caches,
        Set<UUID> nodes,
        int checkFirst,
        int checkThrough,
        boolean checkSizes
    ) {
        this.caches = caches;
        this.checkFirst = checkFirst;
        this.checkThrough = checkThrough;
        this.nodes = nodes;
        this.checkSizes = checkSizes;
    }

    /**
     * @return Caches.
     */
    public Set<String> getCaches() {
        return caches;
    }

    /**
     * @return Nodes on which task will run. If {@code null}, task will run on all server nodes.
     */
    public Set<UUID> getNodes() {
        return nodes;
    }

    /**
     * @return checkFirst.
     */
    public int getCheckFirst() {
        return checkFirst;
    }

    /**
     * @return checkThrough.
     */
    public int getCheckThrough() {
        return checkThrough;
    }

    /**
     * Returns whether to check that index size and cache size are same.
     *
     * @return {@code true} if need check that index size and cache size
     *      are same.
     */
    public boolean isCheckSizes() {
        return checkSizes;
    }

    /** {@inheritDoc} */
    @Override protected void writeExternalData(ObjectOutput out) throws IOException {
        U.writeCollection(out, caches);
        out.writeInt(checkFirst);
        out.writeInt(checkThrough);
        U.writeCollection(out, nodes);
        out.writeBoolean(checkSizes);
    }

    /** {@inheritDoc} */
    @Override protected void readExternalData(byte protoVer, ObjectInput in) throws IOException, ClassNotFoundException {
        caches = U.readSet(in);

        if (protoVer > V1) {
            checkFirst = in.readInt();
            checkThrough = in.readInt();
        }

        if (protoVer > V2)
            nodes = U.readSet(in);

        if (protoVer > V3)
            checkSizes = in.readBoolean();
    }

    /** {@inheritDoc} */
    @Override public byte getProtocolVersion() {
        return V4;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(VisorValidateIndexesTaskArg.class, this);
    }
}
