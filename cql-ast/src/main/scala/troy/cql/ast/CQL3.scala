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
  case class Using(using: String, options: Option[Literals.CqlMap])

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

  case class WhereClause(relations: Seq[WhereClause.Relation])
  /*
<relation> ::= <identifier> <op> <term>
             | '(' <identifier> (',' <identifier>)* ')' <op> <term-tuple>
             | <identifier> IN '(' ( <term> ( ',' <term>)* )? ')'
             | '(' <identifier> (',' <identifier>)* ')' IN '(' ( <term-tuple> ( ',' <term-tuple>)* )? ')'
             | TOKEN '(' <identifier> ( ',' <identifer>)* ')' <op> <term>

<op> ::= '=' | '<' | '>' | '<=' | '>=' | CONTAINS | CONTAINS KEY
<order-by> ::= <ordering> ( ',' <odering> )*
<ordering> ::= <identifer> ( ASC | DESC )?
<term-tuple> ::= '(' <term> (',' <term>)* ')
   */
  object WhereClause {
    type TermTuple = Seq[Term]

    trait Token

    trait Operator
    object Operator {
      case object Equals extends Operator
      case object LessThan extends Operator
      case object GreaterThan extends Operator
      case object LessThanOrEqual extends Operator
      case object GreaterThanOrEqual extends Operator
      case object Contains extends Operator
      case object ContainsKey extends Operator
    }

    trait Relation
    object Relation {
      case class Simple(identifier: String, operator: Operator, term: Term) extends Relation
      case class Tupled(identifiers: Seq[String], operator: Operator, term: TermTuple) extends Relation
      case class MultiValue(identifier: String, term: Seq[Term]) extends Relation
      case class TupledMultiValue(identifiers: Seq[String], term: Seq[TermTuple]) extends Relation
      case class WithToken(token: Token, identifiers: Seq[String], operator: Operator, term: Seq[TermTuple]) extends Relation
    }
  }

  case class OrderBy(orderings: Seq[OrderBy.Ordering])
  object OrderBy {
    trait Direction
    case object Ascending
    case object Descending

    case class Ordering(identifier: String, direction: Option[Direction])
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

sealed trait Term
object Term {
  case class Constant(raw: String) extends Term // TODO

  sealed trait Variable extends Term
  object Variable {
    case object Anonymous extends Variable
    case class Named(name: String) extends Variable
  }
}

object Literals {
  type CqlMap = Map[Term, Term]
}
