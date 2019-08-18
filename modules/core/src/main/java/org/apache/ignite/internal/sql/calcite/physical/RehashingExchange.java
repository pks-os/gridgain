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
package org.apache.ignite.internal.sql.calcite.physical;

import java.util.List;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelDistribution;
import org.apache.calcite.rel.RelDistributionTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelWriter;
import org.apache.calcite.rel.SingleRel;

/**
 * TODO: Add class description.
 */
public class RehashingExchange extends SingleRel implements IgniteRel {

    private final RelDistribution hashDist;

    /**
     * Creates a <code>SingleRel</code>.
     *
     * @param cluster Cluster this relational expression belongs to
     * @param traits
     * @param input Input relational expression
     */
    protected RehashingExchange(RelDistribution hashDist, RelOptCluster cluster, RelTraitSet traits,
        RelNode input) {
        super(cluster, traits, input);
        this.hashDist = hashDist;
    }

    @Override
    public RelNode copy(RelTraitSet traitSet, List<RelNode> inputs) {
        return new RehashingExchange(hashDist, getCluster(), traitSet, sole(inputs));
    }

    @Override public RelWriter explainTerms(RelWriter pw) {
        return super.explainTerms(pw)
            .item("from", getInput().getTraitSet().getTrait(RelDistributionTraitDef.INSTANCE))
            .item("to", hashDist);
    }

//    @Override public RelOptCost computeSelfCost(RelOptPlanner planner, RelMetadataQuery mq) {
//        return super.computeSelfCost(planner, mq).multiplyBy(10);
//    }
}

