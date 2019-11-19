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

package org.apache.ignite.internal.processors.cache.distributed.dht.atomic;

import java.util.List;
import java.util.UUID;
import javax.cache.processor.EntryProcessor;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.processors.affinity.AffinityAssignment;
import org.apache.ignite.internal.processors.affinity.AffinityTopologyVersion;
import org.apache.ignite.internal.processors.cache.CacheObject;
import org.apache.ignite.internal.processors.cache.GridCacheContext;
import org.apache.ignite.internal.processors.cache.GridCacheOperation;
import org.apache.ignite.internal.processors.cache.KeyCacheObject;
import org.apache.ignite.internal.processors.cache.distributed.dht.GridDhtCacheEntry;
import org.apache.ignite.internal.processors.cache.version.GridCacheVersion;
import org.apache.ignite.internal.util.typedef.internal.CU;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
class GridDhtAtomicSingleUpdateFuture extends GridDhtAtomicAbstractUpdateFuture {
    /** */
    private boolean allUpdated;

    /**
     * @param cctx Cache context.
     * @param writeVer Write version.
     * @param updateReq Update request.
     */
    GridDhtAtomicSingleUpdateFuture(
        GridCacheContext cctx,
        GridCacheVersion writeVer,
        GridNearAtomicAbstractUpdateRequest updateReq
    ) {
        super(cctx, writeVer, updateReq);
    }

    /** {@inheritDoc} */
    @Override protected boolean sendAllToDht() {
        return allUpdated;
    }

    /** {@inheritDoc} */
    @Override protected void addDhtKey(KeyCacheObject key, List<ClusterNode> dhtNodes) {
        if (mappings == null) {
            allUpdated = true;

            mappings = U.newHashMap(dhtNodes.size());
        }
    }

    /** {@inheritDoc} */
    @Override protected void addNearKey(KeyCacheObject key, GridDhtCacheEntry.ReaderId[] readers) {
        if (mappings == null)
            mappings = U.newHashMap(readers.length);
    }

    @Override void version(GridCacheVersion ver, int stripe) {
        this.writeVer = ver;
    }

    /** {@inheritDoc} */
    @Override protected GridDhtAtomicAbstractUpdateRequest createRequest(
        UUID nodeId,
        long futId,
        GridCacheVersion writeVer,
        CacheWriteSynchronizationMode syncMode,
        @NotNull AffinityTopologyVersion topVer,
        long ttl,
        long conflictExpireTime,
        @Nullable GridCacheVersion conflictVer
    ) {
        if (canUseSingleRequest(ttl, conflictExpireTime, conflictVer)) {
            return new GridDhtAtomicSingleUpdateRequest(
                cctx.cacheId(),
                nodeId,
                futId,
                writeVer,
                syncMode,
                topVer,
                updateReq.subjectId(),
                updateReq.taskNameHash(),
                cctx.deploymentEnabled(),
                updateReq.keepBinary(),
                updateReq.skipStore());
        }
        else {
            return new GridDhtAtomicUpdateRequest(
                cctx.cacheId(),
                nodeId,
                futId,
                writeVer,
                syncMode,
                topVer,
                updateReq.subjectId(),
                updateReq.taskNameHash(),
                null,
                cctx.deploymentEnabled(),
                updateReq.keepBinary(),
                updateReq.skipStore(),
                false);
        }
    }

    /**
     * @param ttl TTL.
     * @param conflictExpireTime Conflict expire time.
     * @param conflictVer Conflict version.
     * @return {@code True} if it is possible to use {@link GridDhtAtomicSingleUpdateRequest}.
     */
    private boolean canUseSingleRequest(long ttl,
        long conflictExpireTime,
        @Nullable GridCacheVersion conflictVer) {
        return (ttl == CU.TTL_NOT_CHANGED) &&
            (conflictExpireTime == CU.EXPIRE_TIME_CALCULATE) &&
            conflictVer == null;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridDhtAtomicSingleUpdateFuture.class, this, "super", super.toString());
    }

    @Override protected void flushDelayed() {
        // No-op.
    }

    @Override public void delayedWriteEntry(AffinityAssignment assignment, KeyCacheObject k, CacheObject newVal,
        EntryProcessor<Object, Object, Object> o, long newTtl, long conflictExpireTime, @Nullable GridCacheVersion newConflictVer,
        boolean sndPrevVal, CacheObject prevVal, long updCntr, GridCacheOperation op) {
        // Add immediately.
        addWriteEntry2(
            assignment,
            k,
            newVal,
            o,
            newTtl,
            conflictExpireTime,
            newConflictVer,
            sndPrevVal,
            prevVal,
            updCntr,
            op);
    }
}
