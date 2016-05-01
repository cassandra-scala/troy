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

import com.abdulradi.troy.ast._

import scala.util.{ Failure, Success, Try }

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
  //  def getField(keyspace: String, table: String, column: String): Option[Field]
  //  def getPrimaryKey: Option[PrimaryKey]

  //  def withStatement: Cql3Statement => Try[NonEmptySchema]
}

//case object EmptySchema extends Schema {
//  override val withStatement = {
//    case CreateKeyspace(_, name, _) => ???
//    case _ => ???
//  }
//}
//case class NonEmptySchema(schema: Map[CreateKeyspace, Seq[CreateTable]]) extends Schema  {
//
//  override val withStatement = {
//    case s: CreateKeyspace => withKeyspace(s)
//    case s: CreateTable => withTable(s)
//    case _ => ???
//  }
//
//  def keyspaceExists(keyspaceName: KeyspaceName) =
//    schema.keys.exists(_.keyspaceName == keyspaceName)
//
//  def withKeyspace(keyspace: CreateKeyspace) =
//    if (keyspaceExists(keyspace.keyspaceName))
//      ???
//    else
//      ???
//
//  def withTable(table: CreateTable) = ???
//
//}
//object NonEmptySchema {
//  def apply(keyspace: CreateKeyspace) =
//    NonEmptySchema(Map(keyspace -> Seq.empty))
//}

object Schema {
  //  def apply(statements: Seq[DataDefinition]): Try[Schema] = for {
  //    enrichedStatements <- enrichWithContext(statements)
  //    statementsByKeyspace = groupByKeyspace(enrichedStatements)
  //    (k, v) <- statementsByKeyspace
  //    (keyspaceStatements, tableStatements) = splitStatements(enrichedStatements)
  //    tables <- ???
  //    keyspaces <- ???
  //  } yield keyspaces

  val keyspaceOfStatement: DataDefinition => Option[KeyspaceName] = {
    case keyspace: CreateKeyspace => Some(keyspace.keyspaceName)
    case table: CreateTable       => table.tableName.keyspace
    //    case index: CreateIndex => index.tableName.keyspace
  }

  private def groupByKeyspace(statements: Seq[DataDefinition]) =
    statements.groupBy(keyspaceOfStatement)

  //  private def splitStatements(statements: Seq[DataDefinition]): (Seq[CreateKeyspace], Seq[CreateTable]) = {
  //    val grouped = statements.groupBy(_.getClass)
  //
  //    def getGroupAs[T <: DataDefinition]: Seq[T] =
  //      grouped.get(classOf[T]).map(_.asInstanceOf[T]).get //OrElse(Seq.empty[T])
  //
  //    (grouped.get(classOf[CreateKeyspace]).map(_.asInstanceOf[CreateKeyspace]))
  //    ???
  //  }

  /*
   * Accepts data-definition statements including `use` statements
   * Returns similar statements without `use` statement,
   * but with all statements enriched with the current keyspace info
   */
  private def enrichWithContext(statements: Seq[DataDefinition]): Try[Seq[DataDefinition]] = {
    def traverse(enriched: Seq[DataDefinition], remaining: Seq[DataDefinition], context: Option[KeyspaceName]): Try[Seq[DataDefinition]] =
      remaining match {
        case Seq()                              => Success(enriched)
        case UseStatement(keyspaceName) +: tail => traverse(enriched, remaining, Some(keyspaceName)) // Switch current keyspace context
        case (head: CreateTable) +: tail =>
          enrich(head, context)
            .map(statement => traverse(statement +: enriched, tail, context))
            .getOrElse(Failure(new Exception(s"Can't detect the keyspace of table ${head.tableName.table}")))
        case head +: tail => // Pass other statements without enriching
          traverse(head +: enriched, tail, context)
      }

    def enrich(statement: CreateTable, context: Option[KeyspaceName]): Option[DataDefinition] =
      statement.tableName.keyspace.orElse(context).map(setKeyspace(statement, _))

    def setKeyspace(statement: CreateTable, keyspace: KeyspaceName): CreateTable =
      statement.copy(tableName = statement.tableName.copy(keyspace = Some(keyspace)))

    traverse(Seq.empty, statements, None)
  }

}

// TODO: Shapeless
case class TypedQuery[Partition, Row](partitionFields: Partition, rowFields: Row, keyspace: String, table: String)