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

package org.apache.ignite.internal.pagemem.wal.record.delta;

import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.pagemem.PageIdUtils;
import org.apache.ignite.internal.pagemem.PageMemory;
import org.apache.ignite.internal.processors.cache.persistence.tree.io.PagePartitionMetaIO;
import org.apache.ignite.internal.util.typedef.internal.S;

/**
 * Partition meta page delta record.
 * Contains reference to update counters gaps.
 */
public class MetaPageUpdatePartitionDataRecordV2 extends MetaPageUpdatePartitionDataRecord {
    /** */
    private long gapsLink;

    /**
     * @param grpId Group id.
     * @param pageId Page id.
     * @param updateCntr Update counter.
     * @param globalRmvId Global remove id.
     * @param partSize Partition size.
     * @param cntrsPageId Cntrs page id.
     * @param state State.
     * @param allocatedIdxCandidate Allocated index candidate.
     * @param gapsLink Link.
     */
    public MetaPageUpdatePartitionDataRecordV2(
        int grpId,
        long pageId,
        long updateCntr,
        long globalRmvId,
        int partSize,
        long cntrsPageId,
        byte state,
        int allocatedIdxCandidate,
        long gapsLink
    ) {
        super(grpId, pageId, updateCntr, globalRmvId, partSize, cntrsPageId, state, allocatedIdxCandidate);
        this.gapsLink = gapsLink;
    }

    /**
     * @param in Input.
     */
    public MetaPageUpdatePartitionDataRecordV2(DataInput in) throws IOException {
        super(in);

        this.gapsLink = in.readLong();
    }

    /** {@inheritDoc} */
    @Override public void applyDelta(PageMemory pageMem, long pageAddr) throws IgniteCheckedException {
        super.applyDelta(pageMem, pageAddr);

        PagePartitionMetaIO io = PagePartitionMetaIO.VERSIONS.forPage(pageAddr);

        io.setGapsLink(pageAddr, gapsLink);
    }

    /**
     *
     */
    public long gapsLink() {
        return gapsLink;
    }

    /** {@inheritDoc} */
    @Override public void toBytes(ByteBuffer buf) {
        super.toBytes(buf);

        buf.putLong(gapsLink());
    }

    /** {@inheritDoc} */
    @Override public RecordType type() {
        return RecordType.PARTITION_META_PAGE_UPDATE_COUNTERS_V2;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(MetaPageUpdatePartitionDataRecordV2.class, this, "partId", PageIdUtils.partId(pageId()), "super", super.toString());
    }
}
