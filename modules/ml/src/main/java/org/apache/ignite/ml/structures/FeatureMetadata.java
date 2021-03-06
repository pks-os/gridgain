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

package org.apache.ignite.ml.structures;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/** Class for feature metadata. */
public class FeatureMetadata implements Serializable, Externalizable {
    /** Feature name */
    private String name;

    /**
     * Default constructor (required by Externalizable).
     */
    public FeatureMetadata() {
    }

    /**
     * Creates an instance of Feature Metadata class.
     *
     * @param name Name.
     */
    public FeatureMetadata(String name) {
        this.name = name;
    }

    /** */
    public String name() {
        return name;
    }

    /** */
    public void setName(String name) {
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FeatureMetadata metadata = (FeatureMetadata)o;

        return name != null ? name.equals(metadata.name) : metadata.name == null;
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    /** {@inheritDoc} */
    @Override public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(name);
    }

    /** {@inheritDoc} */
    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        name = (String)in.readObject();
    }
}
