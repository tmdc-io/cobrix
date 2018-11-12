/*
 * Copyright 2018 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.cobrix.spark.cobol.source.integration

import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import org.scalatest.FunSuite
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import scala.collection.JavaConversions._

//noinspection NameBooleanParameters
class CobolIntegration5Spec extends FunSuite with SparkTestBase {

  private val exampleName = "Test5(multisegment)"
  private val inputCopybookPath = "file://../data/test5_copybook.cob"
  private val inpudDataPath = "../data/test5_data"

  test(s"Integration test on $exampleName - segment ids") {
    import spark.implicits._

    val expectedSchemaPath = "../data/test5_expected/test5_schema.json"
    val actualSchemaPath = "../data/test5_expected/test5_schema_actual.json"
    val expectedResultsPath = "../data/test5_expected/test5.txt"
    val actualResultsPath = "../data/test5_expected/test5_actual.txt"

    val df = spark
      .read
      .format("cobol")
      .option("copybook", inputCopybookPath)
      .option("is_xcom", "true")
      .option("segment_field", "SEGMENT_ID")
      .option("segment_id_level0", "S01L1")
      .option("segment_id_level1", "S01L2")
      .option("generate_record_id", "true")
      .option("schema_retention_policy", "collapse_root")
      .load(inpudDataPath)

    // This is to print the actual output
    //println(df.schema.json)
    //df.toJSON.take(60).foreach(println)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = df.schema.json

    if (actualSchema != expectedSchema) {
      val writer = new PrintWriter(actualSchemaPath)
      try {
        writer.write(actualSchema)
      } finally {
        writer.close()
      }
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    val actualDf = df
      .orderBy("File_Id", "Record_Id")
      .toJSON
      .take(60)

    val writer = new PrintWriter(actualResultsPath)
    try {
      for (str <- actualDf) {
        writer.write(str)
        writer.write("\n")
      }
    } finally {
      writer.close()
    }

    // toList is used to convert the Java list to Scala list. If it is skipped the resulting type will be Array[AnyRef] instead of Array[String]
    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).toList.toArray
    val actual = Files.readAllLines(Paths.get(actualResultsPath), StandardCharsets.ISO_8859_1).toList.toArray

    if (!actual.sameElements(expected)) {
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to " +
        s"$actualResultsPath for details.")
    }
    Files.delete(Paths.get(actualResultsPath))

  }

  test(s"Integration test on $exampleName - root segment id") {
    import spark.implicits._

    // In this test we check that if only one segment id is specified the root segment ids should
    // still be correctly generated.

    val expectedSchemaPath = "../data/test5_expected/test5a_schema.json"
    val actualSchemaPath = "../data/test5_expected/test5a_schema_actual.json"
    val expectedResultsPath = "../data/test5_expected/test5a.txt"
    val actualResultsPath = "../data/test5_expected/test5a_actual.txt"

    val df = spark
      .read
      .format("cobol")
      .option("copybook", inputCopybookPath)
      .option("is_xcom", "true")
      .option("segment_field", "SEGMENT_ID")
      .option("segment_id_level0", "S01L1")
      .option("generate_record_id", "true")
      .option("schema_retention_policy", "collapse_root")
      .load(inpudDataPath)

    // This is to print the actual output
    //println(df.schema.json)
    //df.toJSON.take(60).foreach(println)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = df.schema.json

    if (actualSchema != expectedSchema) {
      val writer = new PrintWriter(actualSchemaPath)
      try {
        writer.write(actualSchema)
      } finally {
        writer.close()
      }
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    //df.show(200)
    val actualDf = df
      .orderBy("File_Id", "Record_Id")
      .toJSON
      .take(60)

    val writer = new PrintWriter(actualResultsPath)
    try {
      for (str <- actualDf) {
        writer.write(str)
        writer.write("\n")
      }
    } finally {
      writer.close()
    }

    // toList is used to convert the Java list to Scala list. If it is skipped the resulting type will be Array[AnyRef] instead of Array[String]
    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).toList.toArray
    val actual = Files.readAllLines(Paths.get(actualResultsPath), StandardCharsets.ISO_8859_1).toList.toArray

    if (!actual.sameElements(expected)) {
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to " +
        s"$actualResultsPath for details.")
    }
    Files.delete(Paths.get(actualResultsPath))

  }

}