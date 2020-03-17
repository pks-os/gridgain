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

package org.apache.ignite.internal.managers.systemview.walker;

import java.util.UUID;
import org.apache.ignite.lang.IgniteUuid;
import org.apache.ignite.spi.systemview.view.ServiceView;
import org.apache.ignite.spi.systemview.view.SystemViewRowAttributeWalker;

/**
 * Generated by {@code org.apache.ignite.codegen.SystemViewRowAttributeWalkerGenerator}.
 * {@link ServiceView} attributes walker.
 * 
 * @see ServiceView
 */
public class ServiceViewWalker implements SystemViewRowAttributeWalker<ServiceView> {
    /** {@inheritDoc} */
    @Override public void visitAll(AttributeVisitor v) {
        v.accept(0, "serviceId", IgniteUuid.class);
        v.accept(1, "name", String.class);
        v.accept(2, "serviceClass", Class.class);
        v.accept(3, "cacheName", String.class);
        v.accept(4, "originNodeId", UUID.class);
        v.accept(5, "totalCount", int.class);
        v.accept(6, "maxPerNodeCount", int.class);
        v.accept(7, "affinityKey", String.class);
        v.accept(8, "nodeFilter", Class.class);
        v.accept(9, "staticallyConfigured", boolean.class);
    }

    /** {@inheritDoc} */
    @Override public void visitAll(ServiceView row, AttributeWithValueVisitor v) {
        v.accept(0, "serviceId", IgniteUuid.class, row.serviceId());
        v.accept(1, "name", String.class, row.name());
        v.accept(2, "serviceClass", Class.class, row.serviceClass());
        v.accept(3, "cacheName", String.class, row.cacheName());
        v.accept(4, "originNodeId", UUID.class, row.originNodeId());
        v.acceptInt(5, "totalCount", row.totalCount());
        v.acceptInt(6, "maxPerNodeCount", row.maxPerNodeCount());
        v.accept(7, "affinityKey", String.class, row.affinityKey());
        v.accept(8, "nodeFilter", Class.class, row.nodeFilter());
        v.acceptBoolean(9, "staticallyConfigured", row.staticallyConfigured());
    }

    /** {@inheritDoc} */
    @Override public int count() {
        return 10;
    }
}