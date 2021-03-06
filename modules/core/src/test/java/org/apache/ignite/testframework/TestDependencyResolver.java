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

package org.apache.ignite.testframework;

import org.apache.ignite.internal.processors.resource.DependencyResolver;
import org.apache.ignite.internal.processors.resource.NoopDependencyResolver;

/**
 * Test implementation of dependency resolver. You can pass your logic via method reference.
 */
public class TestDependencyResolver extends AbstractTestDependencyResolver {
    /** Resolver. */
    private final DependencyResolver resolver;

    /**
     * @param resolver Method reference.
     */
    public TestDependencyResolver(DependencyResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Default constructor.
     */
    public TestDependencyResolver() {
        this.resolver = new NoopDependencyResolver();
    }

    /** {@inheritDoc} */
    @Override protected <T> T doResolve(T instance) {
        return resolver.resolve(instance);
    }
}
