/*
 * Copyright 2016 Tamer AbdulRadi
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

package com.abdulradi.troy.schema

import com.abdulradi.troy.ast.{ DataType, DataDefinition, TableName }

trait FieldLevel
object FieldLevel {
  case object Partition extends FieldLevel // Partition keys, and static columns
  case object Row extends FieldLevel // Clustering columns, and other fields
}

case class Field(name: String, ftype: DataType, fLevel: FieldLevel)
case class Table(partitionKeys: Seq[Field], clusteringColumns: Seq[Field], staticColumns: Seq[Field], otherColumns: Seq[Field])
case class Keyspace(name: String, tables: Seq[Table])

case class ClusteringColumn(column: Field, direction: String)
case class PrimaryKey(partitionKeys: Seq[Field], clusteringColumns: Seq[ClusteringColumn])
trait Schema {
  def getField(keyspace: String, table: String, column: String): Option[Field]
  def getPrimaryKey: Option[PrimaryKey]
}

object Schema {
  def apply(statements: Seq[DataDefinition]): Schema = ???
}

// TODO: Shapeless
case class TypedQuery[Partition, Row](partitionFields: Partition, rowFields: Row, keyspace: String, table: String)