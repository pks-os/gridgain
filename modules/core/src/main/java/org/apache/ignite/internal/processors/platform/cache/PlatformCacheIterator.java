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

package org.apache.ignite.internal.processors.platform.cache;

import java.util.Iterator;
import javax.cache.Cache;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.binary.BinaryRawWriterEx;
import org.apache.ignite.internal.processors.platform.PlatformAbstractTarget;
import org.apache.ignite.internal.processors.platform.PlatformContext;

/**
 * Interop cache iterator.
 */
public class PlatformCacheIterator extends PlatformAbstractTarget {
    /** Operation: next entry. */
    private static final int OP_NEXT = 1;

    /** Iterator. */
    private final Iterator<Cache.Entry> iter;

    /**
     * Constructor.
     *
     * @param platformCtx Context.
     * @param iter Iterator.
     */
    public PlatformCacheIterator(PlatformContext platformCtx, Iterator<Cache.Entry> iter) {
        super(platformCtx);

        this.iter = iter;
    }

    /** {@inheritDoc} */
    @Override public void processOutStream(int type, BinaryRawWriterEx writer) throws IgniteCheckedException {
        switch (type) {
            case OP_NEXT:
                if (iter.hasNext()) {
                    Cache.Entry e = iter.next();

                    assert e != null;

                    writer.writeBoolean(true);

                    writer.writeObjectDetached(e.getKey());
                    writer.writeObjectDetached(e.getValue());
                }
                else
                    writer.writeBoolean(false);

                break;

            default:
                super.processOutStream(type, writer);
        }
    }
}
