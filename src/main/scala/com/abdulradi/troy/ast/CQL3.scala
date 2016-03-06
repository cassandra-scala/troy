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

package com.abdulradi.troy.ast

trait Cql3Statement
trait DataDefinition
trait Manipulation

case class CreateKeyspace(
  ifNotExists: Boolean,
  keyspaceName: KeyspaceName,
  properties: Seq[CreateKeyspace.KeyspaceOption]
) extends DataDefinition
object CreateKeyspace {
  trait KeyspaceOption
  case class Replication(options: Seq[(String, String)]) extends KeyspaceOption // TODO
}

case class UseStatement(keyspaceName: KeyspaceName) extends DataDefinition
//
//trait SchemaAlteringStatement
case class CreateTable(
  ifNotExists: Boolean,
  tableName: TableName,
  columns: Seq[CreateTable.Column],
  primaryKey: Option[CreateTable.PrimaryKey],
  options: Seq[CreateTable.CreateTableOption]
) extends DataDefinition

object CreateTable {
  case class Column(name: String, dataType: DataType, isStatic: Boolean, isPrimaryKey: Boolean)
  case class PrimaryKey(partitionKeys: Seq[String], clusteringColumns: Seq[String])

  trait CreateTableOption
  case class Property() extends CreateTableOption
  case object CompactStorage extends CreateTableOption
  case object ClusteringOrder extends CreateTableOption
}
//
//case class KeyspaceReplication(`class`: String, replicationFactor: Int)
//case class CreateKeyspaceStatement(name: String, replication: KeyspaceReplication) extends SchemaAlteringStatement
//
//trait CreateIndexStatement extends SchemaAlteringStatement
//
//trait ModificationStatement extends Statement
//case class Insert extends ModificationStatement
//case class Update extends ModificationStatement
//case class Delete extends ModificationStatement

case class SelectStatement(
  isJson: Boolean,
  selection: SelectStatement.SelectClause,
  from: TableName,
  where: Option[SelectStatement.WhereClause],
  orderBy: Option[SelectStatement.OrderBy],
  limit: Option[Int],
  allowFiltering: Boolean
) extends Cql3Statement
object SelectStatement {
  trait SelectClause
  case class Count(str: String, as: Option[String]) extends SelectClause
  case class Selection(isDistinct: Boolean, selectionList: SelectionList) extends SelectClause

  trait SelectionList
  case object Asterisk extends SelectionList
  case class SelectionItems(items: Seq[SelectionItem]) extends SelectionList

  case class SelectionItem(selector: Selector, as: Option[String])
  sealed trait Selector
  case class Identifier(name: String) extends Selector
  case class WriteTime(identifier: Identifier) extends Selector
  case class Ttl(identifier: Identifier) extends Selector
  case class Function(name: String, params: Seq[Selector]) extends Selector // Non empty

  sealed trait WhereClause

  sealed trait OrderBy
}

trait ConsistencyLevel
object ConsistencyLevel {
  case object Any extends ConsistencyLevel
  case object One extends ConsistencyLevel
  case object Quorum extends ConsistencyLevel
  case object All extends ConsistencyLevel
  case object LocalQuorum extends ConsistencyLevel
  case object EachQuorum extends ConsistencyLevel
}

case class KeyspaceName(name: String)
case class TableName(keyspace: Option[KeyspaceName], table: String)