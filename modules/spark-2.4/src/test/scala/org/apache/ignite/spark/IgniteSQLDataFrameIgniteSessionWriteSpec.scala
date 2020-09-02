/*
 * Copyright 2020 GridGain Systems, Inc. and Contributors.
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

package org.apache.ignite.spark

import org.apache.ignite.internal.IgnitionEx
import org.apache.ignite.spark.AbstractDataFrameSpec.{TEST_CONFIG_FILE, enclose}
import org.apache.ignite.spark.IgniteDataFrameSettings._
import org.apache.ignite.testframework.GridTestUtils.resolveIgnitePath
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.functions._
import org.apache.spark.sql.ignite.IgniteSparkSession
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
  */
@RunWith(classOf[JUnitRunner])
class IgniteSQLDataFrameIgniteSessionWriteSpec extends IgniteSQLDataFrameWriteSpec {
    describe("Additional features for IgniteSparkSession") {
        // TODO: fix it IGNITE-12435
        ignore("Save data frame as a existing table with saveAsTable('table_name') - Overwrite") {
            val citiesDataFrame = spark.read.json(
                resolveIgnitePath("modules/spark-2.4/src/test/resources/cities.json").getAbsolutePath)

            citiesDataFrame.write
                .format(FORMAT_IGNITE)
                .option(OPTION_CONFIG_FILE, TEST_CONFIG_FILE)
                .option(OPTION_CREATE_TABLE_PRIMARY_KEY_FIELDS, "id")
                .option(OPTION_CREATE_TABLE_PARAMETERS, "template=replicated")
                .mode(SaveMode.Overwrite)
                .saveAsTable("city")

            assert(rowsCount("city") == citiesDataFrame.count(),
                s"Table json_city should contain data from json file.")
        }

        // TODO: fix it IGNITE-12435
        ignore("Save data frame as a existing table with saveAsTable('table_name') - Append") {
            val citiesDataFrame = spark.read.json(
                resolveIgnitePath("modules/spark-2.4/src/test/resources/cities.json").getAbsolutePath)

            val rowCnt = citiesDataFrame.count()

            citiesDataFrame
                .withColumn("id", col("id") + rowCnt) //Edit id column to prevent duplication
                .write
                .format(FORMAT_IGNITE)
                .option(OPTION_CONFIG_FILE, TEST_CONFIG_FILE)
                .option(OPTION_CREATE_TABLE_PRIMARY_KEY_FIELDS, "id")
                .option(OPTION_CREATE_TABLE_PARAMETERS, "template=replicated")
                .mode(SaveMode.Append)
                .partitionBy("id")
                .saveAsTable("city")

            assert(rowsCount("city") == rowCnt*2,
                s"Table json_city should contain data from json file.")
        }

        // TODO: fix it IGNITE-12435
        ignore("Save data frame as a new table with saveAsTable('table_name')") {
            val citiesDataFrame = spark.read.json(
                resolveIgnitePath("modules/spark-2.4/src/test/resources/cities.json").getAbsolutePath)

            citiesDataFrame.write
                .format(FORMAT_IGNITE)
                .option(OPTION_CONFIG_FILE, TEST_CONFIG_FILE)
                .option(OPTION_CREATE_TABLE_PRIMARY_KEY_FIELDS, "id")
                .option(OPTION_CREATE_TABLE_PARAMETERS, "template=replicated")
                .saveAsTable("new_cities")

            assert(rowsCount("new_cities") == citiesDataFrame.count(),
                s"Table json_city should contain data from json file.")
        }
    }

    override protected def createSparkSession(): Unit = {
        val configProvider = enclose(null) (x ⇒ () ⇒ {
            val cfg = IgnitionEx.loadConfiguration(TEST_CONFIG_FILE).get1()

            cfg.setClientMode(true)

            cfg.setIgniteInstanceName("client-2")

            cfg
        })

        spark = IgniteSparkSession.builder()
            .appName("DataFrameSpec")
            .master("local")
            .config("spark.executor.instances", "2")
            .igniteConfigProvider(configProvider)
            .getOrCreate()
    }
}
