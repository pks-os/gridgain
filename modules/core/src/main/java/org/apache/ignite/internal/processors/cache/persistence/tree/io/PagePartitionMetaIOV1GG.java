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

package org.apache.ignite.internal.processors.cache.persistence.tree.io;

import org.apache.ignite.internal.pagemem.PageUtils;
import org.apache.ignite.internal.util.GridStringBuilder;

/**
 * IO for partition metadata pages.
 * Add UpdateLogTree (update counter -> row link) for each partition.
 */
public class PagePartitionMetaIOV1GG extends PagePartitionMetaIOV2 implements PagePartitionMetaIOGG {

    /** */
    private final int updateLogTreeRootOff;

    /**
     * @param ver Version.
     */
    public PagePartitionMetaIOV1GG(int ver) {
        this(ver, GAPS_LINK + 8);
    }

    /**
     * @param ver Version.
     * @param fieldOffset Offset after which the page fields are written.
     */
    public PagePartitionMetaIOV1GG(int ver, int fieldOffset) {
        super(ver);

        updateLogTreeRootOff = fieldOffset;
    }

    /** {@inheritDoc} */
    @Override public void initNewPage(long pageAddr, long pageId, int pageSize) {
        super.initNewPage(pageAddr, pageId, pageSize);

        initSpecificFields(pageAddr, pageId, pageSize);
    }

    /** {@inheritDoc} */
    @Override public void initSpecificFields(long pageAddr, long pageId, int pageSize) {
        setUpdateTreeRoot(pageAddr, 0L);
    }

    /** {@inheritDoc} */
    @Override public long getUpdateTreeRoot(long pageAddr) {
        return PageUtils.getLong(pageAddr, updateLogTreeRootOff);
    }

    /** {@inheritDoc} */
    @Override public void setUpdateTreeRoot(long pageAddr, long link) {
        PageUtils.putLong(pageAddr, updateLogTreeRootOff, link);
    }

    /** {@inheritDoc} */
    @Override protected void printFields(long pageAddr, GridStringBuilder sb) {
        super.printFields(pageAddr, sb);

        specificFields(pageAddr, sb);
    }

    /** {@inheritDoc} */
    @Override public void specificFields(long pageAddr, GridStringBuilder sb) {
        sb.a(",\n\tupdLogRootPageId=").a(getUpdateTreeRoot(pageAddr));
    }

    /** {@inheritDoc} */
    @Override public void upgradePage(long pageAddr) {
        assert PageIO.getType(pageAddr) == getType();

        int from = PageIO.getVersion(pageAddr);

        assert from < 3 : "Unexpected page IO version [ver=" + from + ']';

        PageIO.setVersion(pageAddr, getVersion());

        if (from < 2) {
            setPendingTreeRoot(pageAddr, 0);
            setPartitionMetaStoreReuseListRoot(pageAddr, 0);
            setGapsLink(pageAddr, 0);
        }

        setUpdateTreeRoot(pageAddr, 0);
    }
}
