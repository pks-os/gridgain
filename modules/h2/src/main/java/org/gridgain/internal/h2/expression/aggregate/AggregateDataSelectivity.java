/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.gridgain.internal.h2.expression.aggregate;

import org.gridgain.internal.h2.engine.Constants;
import org.gridgain.internal.h2.engine.Database;
import org.gridgain.internal.h2.engine.Session;
import org.gridgain.internal.h2.util.IntIntHashMap;
import org.gridgain.internal.h2.value.Value;
import org.gridgain.internal.h2.value.ValueInt;

/**
 * Data stored while calculating a SELECTIVITY aggregate.
 */
class AggregateDataSelectivity extends AggregateData {

    private final boolean distinct;

    private long count;
    private IntIntHashMap distinctHashes;
    private double m2;

    /**
     * Creates new instance of data for SELECTIVITY aggregate.
     *
     * @param distinct if distinct is used
     */
    AggregateDataSelectivity(boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    void add(Session ses, Value v) {
        count++;
        if (distinctHashes == null) {
            distinctHashes = new IntIntHashMap();
        }
        int size = distinctHashes.size();
        if (size > Constants.SELECTIVITY_DISTINCT_COUNT) {
            distinctHashes = new IntIntHashMap();
            m2 += size;
        }
        int hash = v.hashCode();
        // the value -1 is not supported
        distinctHashes.put(hash, 1);
    }

    @Override
    public void mergeAggregate(Session ses, AggregateData agg) {
        throw new UnsupportedOperationException();
    }

    @Override
    Value getValue(Database database, int dataType) {
        if (distinct) {
            count = 0;
        }
        Value v = null;
        int s = 0;
        if (count == 0) {
            s = 0;
        } else {
            m2 += distinctHashes.size();
            m2 = 100 * m2 / count;
            s = (int) m2;
            s = s <= 0 ? 1 : s > 100 ? 100 : s;
        }
        v = ValueInt.get(s);
        return v.convertTo(dataType);
    }

    @Override
    public long getMemory() {
        throw new UnsupportedOperationException();
    }

}
