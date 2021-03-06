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

package org.apache.ignite.spark.impl.optimization

import org.apache.ignite.spark.IgniteContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.catalyst.catalog.CatalogTable
import org.apache.spark.sql.catalyst.expressions.NamedExpression

/**
  * Class to store Ignite query info during optimization process.
  *
  * @param igniteContext IgniteContext.
  * @param sqlContext SQLContext.
  * @param cacheName Cache name.
  * @param aliasIndex Iterator to generate indexes for auto-generated aliases.
  * @param catalogTable CatalogTable from source relation.
  */
case class IgniteQueryContext(
    igniteContext: IgniteContext,
    sqlContext: SQLContext,
    cacheName: String,
    aliasIndex: Iterator[Int],
    catalogTable: Option[CatalogTable] = None,
    distributeJoin: Boolean = false
) {
    /**
      * @return Unique table alias.
      */
    def uniqueTableAlias: String = "table" + aliasIndex.next

    /**
      * @param col Column
      * @return Unique column alias.
      */
    def uniqueColumnAlias(col: NamedExpression): String = col.name + "_" + aliasIndex.next
}
