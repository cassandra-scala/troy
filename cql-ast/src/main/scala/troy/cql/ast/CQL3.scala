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

package troy.cql.ast

import troy.cql.ast.Term.{ BindMarker, TupleLiteral }

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

case class CreateIndex(
  isCustom: Boolean,
  ifNotExists: Boolean,
  indexName: Option[String],
  tableName: TableName,
  identifier: CreateIndex.IndexIdentifier,
  using: Option[CreateIndex.Using]
) extends DataDefinition

object CreateIndex {
  case class Using(using: String, options: Option[Term.MapLiteral])

  trait IndexIdentifier
  case class Identifier(value: String) extends IndexIdentifier
  case class Keys(of: String) extends IndexIdentifier
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
  mod: Option[SelectStatement.Mod],
  selection: SelectStatement.Selection,
  from: TableName,
  where: Option[SelectStatement.WhereClause],
  orderBy: Option[SelectStatement.OrderBy],
  perPartitionLimit: Option[SelectStatement.LimitParam],
  limit: Option[SelectStatement.LimitParam],
  allowFiltering: Boolean
) extends Cql3Statement
object SelectStatement {
  sealed trait Mod
  case object Json extends Mod
  case object Distinct extends Mod

  sealed trait Selection
  case object Asterisk extends Selection
  case class SelectClause(items: Seq[SelectionClauseItem]) extends Selection
  case class SelectionClauseItem(selector: Selector, as: Option[Identifier])

  sealed trait Selector
  case class ColumnName(name: Identifier) extends Selector
  case class SelectTerm(term: Term) extends Selector
  case class Cast(selector: Selector, as: DataType) extends Selector
  case class Function(functionName: FunctionName, params: Seq[Selector]) extends Selector // Non empty
  case object Count extends Selector

  case class WhereClause(relations: Seq[WhereClause.Relation])
  object WhereClause {

    trait Operator
    object Operator {
      case object Equals extends Operator
      case object LessThan extends Operator
      case object GreaterThan extends Operator
      case object LessThanOrEqual extends Operator
      case object GreaterThanOrEqual extends Operator
      case object NotEquals extends Operator
      case object In extends Operator
      case object Contains extends Operator
      case object ContainsKey extends Operator
    }

    trait Relation
    object Relation {
      case class Simple(columnName: ColumnName, operator: Operator, term: Term) extends Relation
      case class Tupled(columnNames: Seq[ColumnName], operator: Operator, term: TupleLiteral) extends Relation
      case class Token(columnNames: Seq[ColumnName], operator: Operator, term: Term) extends Relation
    }

  }

  sealed trait LimitParam
  case class LimitValue(integer: Integer) extends LimitParam
  case class LimitVariable(bindMarker: BindMarker) extends LimitParam

  case class OrderBy(orderings: Seq[OrderBy.Ordering])
  object OrderBy {
    trait Direction
    case object Ascending
    case object Descending

    case class Ordering(columnName: ColumnName, direction: Option[Direction])
  }
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
case class FunctionName(keyspace: Option[KeyspaceName], table: String)

sealed trait Term

object Term {
  case class Constant(raw: String) extends Term
  sealed trait Literal extends Term
  sealed trait CollectionLiteral extends Literal
  case class MapLiteral(pairs: Seq[(Term, Term)]) extends CollectionLiteral
  case class SetLiteral(values: Seq[Term]) extends CollectionLiteral
  case class ListLiteral(values: Seq[Term]) extends CollectionLiteral

  case class UdtLiteral(members: Seq[(Identifier, Term)]) extends Literal
  case class TupleLiteral(values: Seq[Term]) extends Literal

  case class FunctionCall(functionName: Identifier, params: Seq[Term]) extends Term
  case class TypeHint(cqlType: DataType, term: Term) extends Term
  sealed trait BindMarker extends Term
  object BindMarker {
    case object Anonymous extends BindMarker
    case class Named(name: Identifier) extends BindMarker
  }
}