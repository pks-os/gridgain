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

import java.nio.ByteBuffer;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.internal.util.tostring.GridToStringInclude;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.internal.util.typedef.internal.CU;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.plugin.extensions.communication.MessageReader;
import org.apache.ignite.plugin.extensions.communication.MessageWriter;

/**
 *
 */
public class CacheEntryPredicateContainsValue extends CacheEntryPredicateAdapter {
    /** */
    private static final long serialVersionUID = 0L;

    /** */
    @GridToStringInclude
    private CacheObject val;

    /**
     * Required by {@link org.apache.ignite.plugin.extensions.communication.Message}.
     */
    public CacheEntryPredicateContainsValue() {
        // No-op.
    }

    /**
     *
     * @param val Value to compare with.
     */
    public CacheEntryPredicateContainsValue(CacheObject val) {
        assert val != null;

        this.val = val;
    }

    /** {@inheritDoc} */
    @Override public boolean apply(GridCacheEntryEx e) {
        CacheObject val = peekVisibleValue(e);

        if (this.val == null && val == null)
            return true;

        if (this.val == null || val == null)
            return false;

        GridCacheContext cctx = e.context();

        if (this.val instanceof BinaryObject && val instanceof BinaryObject)
            return F.eq(val, this.val);

        Object thisVal = CU.value(this.val, cctx, false);
        Object cacheVal = CU.value(val, cctx, false);

        return F.eq(thisVal, cacheVal);
    }

    /** {@inheritDoc} */
    @Override public void finishUnmarshal(GridCacheContext ctx, ClassLoader ldr) throws IgniteCheckedException {
        val.finishUnmarshal(ctx.cacheObjectContext(), ldr);
    }

    /** {@inheritDoc} */
    @Override public void prepareMarshal(GridCacheContext ctx) throws IgniteCheckedException {
        val.prepareMarshal(ctx.cacheObjectContext());
    }

    /** {@inheritDoc} */
    @Override public boolean writeTo(ByteBuffer buf, MessageWriter writer) {
        writer.setBuffer(buf);

        if (!super.writeTo(buf, writer))
            return false;

        if (!writer.isHeaderWritten()) {
            if (!writer.writeHeader(directType(), fieldsCount()))
                return false;

            writer.onHeaderWritten();
        }

        switch (writer.state()) {
            case 0:
                if (!writer.writeMessage("val", val))
                    return false;

                writer.incrementState();

        }

        return true;
    }

    /** {@inheritDoc} */
    @Override public boolean readFrom(ByteBuffer buf, MessageReader reader) {
        reader.setBuffer(buf);

        if (!reader.beforeMessageRead())
            return false;

        if (!super.readFrom(buf, reader))
            return false;

        switch (reader.state()) {
            case 0:
                val = reader.readMessage("val");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

        }

        return reader.afterMessageRead(CacheEntryPredicateContainsValue.class);
    }

    /** {@inheritDoc} */
    @Override public short directType() {
        return 98;
    }

    /** {@inheritDoc} */
    @Override public byte fieldsCount() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(CacheEntryPredicateContainsValue.class, this);
    }
}
