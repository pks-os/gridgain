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

package org.apache.ignite.internal.processors.query.h2.sql;

import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.gridgain.internal.h2.expression.Expression;
import org.gridgain.internal.h2.table.Column;
import org.gridgain.internal.h2.value.TypeInfo;
import org.gridgain.internal.h2.value.Value;
import org.gridgain.internal.h2.value.ValueBoolean;
import org.gridgain.internal.h2.value.ValueDouble;
import org.gridgain.internal.h2.value.ValueLong;

/**
 * SQL Data type based on H2.
 */
public final class GridSqlType {
    /** */
    public static final GridSqlType UNKNOWN = new GridSqlType(Value.UNKNOWN, 0, 0, 0, null);

    /** */
    public static final GridSqlType BIGINT = new GridSqlType(Value.LONG, 0, ValueLong.PRECISION,
        ValueLong.DISPLAY_SIZE, "BIGINT");

    /** */
    public static final GridSqlType DOUBLE = new GridSqlType(Value.DOUBLE, 0, ValueDouble.PRECISION,
        ValueDouble.DISPLAY_SIZE, "DOUBLE");

    /** */
    public static final GridSqlType UUID = new GridSqlType(Value.UUID, 0, Integer.MAX_VALUE, 36, "UUID");

    /** */
    public static final GridSqlType BOOLEAN = new GridSqlType(Value.BOOLEAN, 0, ValueBoolean.PRECISION,
        ValueBoolean.DISPLAY_SIZE, "BOOLEAN");

    /** */
    public static final GridSqlType STRING = new GridSqlType(Value.STRING, 0, 0,
        -1, "VARCHAR");

    /** */
    public static final GridSqlType RESULT_SET = new GridSqlType(Value.RESULT_SET, 0,
        Integer.MAX_VALUE, Integer.MAX_VALUE, "");

    /** H2 type. */
    private final int type;

    /** */
    private final int scale;

    /** */
    private final long precision;

    /** */
    private final int displaySize;

    /** */
    private final String sql;

    /**
     * @param type H2 Type.
     * @param scale Scale.
     * @param precision Precision.
     * @param displaySize Display size.
     * @param sql SQL definition of the type.
     */
    private GridSqlType(int type, int scale, long precision, int displaySize, String sql) {
        assert !F.isEmpty(sql) || type == Value.UNKNOWN || type == Value.RESULT_SET;

        this.type = type;
        this.scale = scale;
        this.precision = precision;
        this.displaySize = displaySize;
        this.sql = sql;
    }

    /**
     * @param c Column to take type definition from.
     * @return Type.
     */
    public static GridSqlType fromColumn(Column c) {
        TypeInfo type = c.getType();

        if (c.getName() != null)
            c = new Column(null, type);

        return new GridSqlType(type.getValueType(), type.getScale(),
            type.getPrecision(), type.getDisplaySize(), c.getCreateSQL());
    }

    /**
     * @param e Expression to take type from.
     * @return Type.
     */
    public static GridSqlType fromExpression(Expression e) {
        if (e.getType().getValueType() == Value.UNKNOWN)
            return UNKNOWN;

        return fromColumn(new Column(null, e.getType()));
    }

    /**
     * @return Get H2 type.
     */
    public int type() {
        return type;
    }

    /**
     * Get the scale of this expression.
     *
     * @return Scale.
     */
    public int scale() {
        return scale;
    }

    /**
     * Get the precision of this expression.
     *
     * @return Precision.
     */
    public long precision() {
        return precision;
    }

    /**
     * Get the display size of this expression.
     *
     * @return the display size
     */
    public int displaySize() {
        return displaySize;
    }

    /**
     * @return SQL definition of the type.
     */
    public String sql() {
        return sql;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridSqlType.class, this);
    }
}