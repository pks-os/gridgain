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

package org.apache.ignite.internal.processors.cache;

import org.apache.ignite.cache.affinity.AffinityKeyMapper;
import org.apache.ignite.internal.GridKernalContext;
import org.apache.ignite.internal.processors.cache.compress.EntryCompressionStrategy;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public class CacheObjectContext implements CacheObjectValueContext {
    /** */
    private final GridKernalContext kernalCtx;

    /** */
    private final String cacheName;

    /** */
    @SuppressWarnings("deprecation")
    private AffinityKeyMapper dfltAffMapper;

    /** Whether custom affinity mapper is used. */
    private final boolean customAffMapper;

    /** */
    private final boolean cpyOnGet;

    /** */
    private final boolean storeVal;

    /** */
    private final boolean addDepInfo;

    /** Binary enabled flag. */
    private final boolean binaryEnabled;

    /** Cache entry compression implementation. */
    private volatile EntryCompressionStrategy compressionStrategy;

    /**
     * @param kernalCtx Kernal context.
     * @param cacheName Cache name.
     * @param dfltAffMapper Default affinity mapper.
     * @param cpyOnGet Copy on get flag.
     * @param storeVal {@code True} if should store unmarshalled value in cache.
     * @param addDepInfo {@code true} if deployment info should be associated with the objects of this cache.
     * @param binaryEnabled Binary enabled flag.
     */
    @SuppressWarnings("deprecation")
    public CacheObjectContext(GridKernalContext kernalCtx,
        String cacheName,
        AffinityKeyMapper dfltAffMapper,
        boolean customAffMapper,
        boolean cpyOnGet,
        boolean storeVal,
        boolean addDepInfo,
        boolean binaryEnabled) {
        this.kernalCtx = kernalCtx;
        this.cacheName = cacheName;
        this.dfltAffMapper = dfltAffMapper;
        this.customAffMapper = customAffMapper;
        this.cpyOnGet = cpyOnGet;
        this.storeVal = storeVal;
        this.addDepInfo = addDepInfo;
        this.binaryEnabled = binaryEnabled;
    }

    /**
     * @return Cache name.
     */
    public String cacheName() {
        return cacheName;
    }

    /** {@inheritDoc} */
    @Override public boolean addDeploymentInfo() {
        return addDepInfo;
    }

    /** {@inheritDoc} */
    @Override public boolean copyOnGet() {
        return cpyOnGet;
    }

    /** {@inheritDoc} */
    @Override public boolean storeValue() {
        return storeVal;
    }

    /**
     * @return Default affinity mapper.
     */
    @SuppressWarnings("deprecation")
    public AffinityKeyMapper defaultAffMapper() {
        return dfltAffMapper;
    }

    /**
     * @return Whether custom affinity mapper is used.
     */
    public boolean customAffinityMapper() {
        return customAffMapper;
    }

    /** {@inheritDoc} */
    @Override public GridKernalContext kernalContext() {
        return kernalCtx;
    }

    /** {@inheritDoc} */
    @Override public boolean binaryEnabled() {
        return binaryEnabled;
    }

    /**
     * Internal method for setting compression strategy.
     *
     * @param compressionStrategy Compression strategy to use with this cache.
     */
    void compressionStrategy(EntryCompressionStrategy compressionStrategy) {
        this.compressionStrategy = compressionStrategy;
    }

    /** {@inheritDoc} */
    @Nullable @Override public EntryCompressionStrategy compressionStrategy() {
        return compressionStrategy;
    }

    /** {@inheritDoc} */
    @Override public boolean compressKeys() {
        return compressionStrategy != null && compressionStrategy.compressKeys();
    }

    /**
     * @param o Object to unwrap.
     * @param keepBinary Keep binary flag.
     * @param cpy Copy value flag.
     * @param ldr Class loader, used for deserialization from binary representation.
     * @return Unwrapped object.
     */
    public Object unwrapBinaryIfNeeded(Object o, boolean keepBinary, boolean cpy, @Nullable ClassLoader ldr) {
        if (o == null)
            return null;

        return CacheObjectUtils.unwrapBinaryIfNeeded(this, o, keepBinary, cpy, ldr);
    }
}
