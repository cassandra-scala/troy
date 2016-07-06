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

package troy.schema

import troy.cql.ast._

import scala.util.Left

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

case class Schema(schema: Map[KeyspaceName, Seq[CreateTable]], context: Option[KeyspaceName]) {
  import Schema._

  def apply(query: SelectStatement): Result[Seq[CreateTable.Column]] = query match {
    case SelectStatement(_, SelectStatement.Selection(_, SelectStatement.SelectionItems(selection)), table, _, _, _, _) =>
      apply(table, selection.map(_.selector).map {
        case SelectStatement.Identifier(name) => name
        case _                                => ???
      })
  }

  def extractVariables(statement: Cql3Statement): Result[Seq[CreateTable.Column]] = statement match {
    case SelectStatement(_, _, from, Some(where), _, _, _) => extractVariables(from, where)
    case SelectStatement(_, _, from, None, _, _, _)        => success(Seq.empty)
    case _                                                 => ???
  }

  def extractVariables(table: TableName, where: SelectStatement.WhereClause): Result[Seq[CreateTable.Column]] =
    apply(table, where.relations.flatMap {
      case SelectStatement.WhereClause.Relation.Simple(identifier, _, Term.Variable.Anonymous) => Seq(identifier)
      case SelectStatement.WhereClause.Relation.Tupled(identifiers, _, _)                      => ???
      case SelectStatement.WhereClause.Relation.MultiValue(identifier, _)                      => ???
      case SelectStatement.WhereClause.Relation.TupledMultiValue(identifiers, _)               => ???
      case SelectStatement.WhereClause.Relation.WithToken(_, identifiers, _, _)                => ???
    })

  def apply(table: TableName, columns: Seq[String]): Result[Seq[CreateTable.Column]] =
    getColumns(table.keyspace, table.table, columns)

  private def resolveKeyspaceName(keyspaceName: Option[KeyspaceName]): Result[KeyspaceName] =
    keyspaceName.orElse(context).toRight("Keyspace not specified")

  private def getKeyspace(keyspaceName: KeyspaceName): Result[Seq[CreateTable]] =
    schema.get(keyspaceName).toRight(s"Keyspace '${keyspaceName.name}' not found")

  private def getKeyspace(keyspaceName: Option[KeyspaceName]): Result[Seq[CreateTable]] =
    for {
      keyspace <- resolveKeyspaceName(keyspaceName).right
      tables <- getKeyspace(keyspace).right
    } yield tables

  private def getTable(keyspace: Seq[CreateTable], table: String): Result[CreateTable] =
    keyspace.find(_.tableName.table == table).toRight(s"Table '$table' not found")

  private def getTable(keyspaceName: Option[KeyspaceName], tableName: String): Result[CreateTable] =
    for {
      tables <- getKeyspace(keyspaceName).right
      table <- getTable(tables, tableName).right
    } yield table

  private def getColumn(table: CreateTable, columnName: String): Result[CreateTable.Column] =
    table.columns.find(_.name == columnName).toRight(s"Column $columnName not found")

  private def getColumns(table: CreateTable, columnNames: Seq[String]): Result[Seq[CreateTable.Column]] =
    Result.seq(columnNames.map(getColumn(table, _)))

  def getColumns(keyspaceName: Option[KeyspaceName], table: String, selectColumns: Seq[String]): Result[Seq[CreateTable.Column]] =
    for {
      table <- getTable(keyspaceName, table).right
      columns <- getColumns(table, selectColumns).right
    } yield columns

  val withStatement: DataDefinition => Result[Schema] = {
    case s: CreateKeyspace => withKeyspace(s)
    case s: CreateTable    => withTable(s)
    case _                 => ???
  }

  def keyspaceExists(keyspaceName: KeyspaceName) =
    schema.keys.exists(_ == keyspaceName)

  private def tableExists(tables: Seq[CreateTable], createTable: CreateTable) =
    tables.exists(_.tableName.table == createTable.tableName.table)

  def withKeyspace(keyspace: CreateKeyspace) =
    if (keyspaceExists(keyspace.keyspaceName))
      fail(s"Keyspace ${keyspace.keyspaceName.name} exists")
    else
      success(copy(schema = schema + (keyspace.keyspaceName -> Seq.empty)))

  def withTable(createTable: CreateTable): Result[Schema] =
    for {
      keyspace <- resolveKeyspaceName(createTable.tableName.keyspace).right
      tables <- getKeyspace(keyspace).right
      _ <- (if (tableExists(tables, createTable)) fail(s"Table ${createTable.tableName} already exists") else success).right
    } yield copy(schema = schema + (keyspace -> (createTable +: tables)))

  //    for {
  //      keyspaceName <- resolveKeyspaceName(createTable.tableName.keyspace).right
  //      tables <- getKeyspace(createTable.tableName.keyspace).right
  //      table <- getTable(tables, createTable.tableName.table).right
  ////      if !tables.exists(_.tableName.table == table.tableName.table)
  //      newTables = table +: tables
  //
  //    } yield
}

object Schema {
  type Result[T] = Either[String, T]
  object Result {
    def seq[T](results: Seq[Result[T]]): Result[Seq[T]] =
      results.collectFirst {
        case Left(e) => fail(e)
      }.getOrElse(success(results.map(_.right.get)))
  }

  val empty: Schema = Schema(Map.empty, None)

  def apply(statements: Seq[DataDefinition]) =
    (success(empty) /: statements) {
      case (Right(schema), statement) => schema.withStatement(statement)
      case (Left(e), _)               => fail(e)
    }

  private def fail[T](msg: String): Result[T] =
    Left(msg)

  private def success[T](result: T): Result[T] =
    Right(result)

  private def success(): Result[_] =
    Right(())

  //    def apply(statements: Seq[DataDefinition]): Try[Schema] = for {
  //      enrichedStatements <- enrichWithContext(statements)
  //      statementsByKeyspace = groupByKeyspace(enrichedStatements)
  //      (k, v) <- statementsByKeyspace
  //      (keyspaceStatements, tableStatements) = splitStatements(enrichedStatements)
  //      tables <- ???
  //      keyspaces <- ???
  //    } yield keyspaces
  //
  //  val keyspaceOfStatement: DataDefinition => Option[KeyspaceName] = {
  //    case keyspace: CreateKeyspace => Some(keyspace.keyspaceName)
  //    case table: CreateTable       => table.tableName.keyspace
  //    //    case index: CreateIndex => index.tableName.keyspace
  //  }
  //
  //  private def groupByKeyspace(statements: Seq[DataDefinition]) =
  //    statements.groupBy(keyspaceOfStatement)

  //  private def splitStatements(statements: Seq[DataDefinition]): (Seq[CreateKeyspace], Seq[CreateTable]) = {
  //    val grouped = statements.groupBy(_.getClass)
  //
  //    def getGroupAs[T <: DataDefinition]: Seq[T] =
  //      grouped.get(classOf[T]).map(_.asInstanceOf[T]).get //OrElse(Seq.empty[T])
  //
  //    (grouped.get(classOf[CreateKeyspace]).map(_.asInstanceOf[CreateKeyspace]))
  //    ???
  //  }

  //  /*
  //   * Accepts data-definition statements including `use` statements
  //   * Returns similar statements without `use` statement,
  //   * but with all statements enriched with the current keyspace info
  //   */
  //  private def enrichWithContext(statements: Seq[DataDefinition]): Try[Seq[DataDefinition]] = {
  //    def traverse(enriched: Seq[DataDefinition], remaining: Seq[DataDefinition], context: Option[KeyspaceName]): Try[Seq[DataDefinition]] =
  //      remaining match {
  //        case Seq()                              => Success(enriched)
  //        case UseStatement(keyspaceName) +: tail => traverse(enriched, remaining, Some(keyspaceName)) // Switch current keyspace context
  //        case (head: CreateTable) +: tail =>
  //          enrich(head, context)
  //            .map(statement => traverse(statement +: enriched, tail, context))
  //            .getOrElse(Failure(new Exception(s"Can't detect the keyspace of table ${head.tableName.table}")))
  //        case head +: tail => // Pass other statements without enriching
  //          traverse(head +: enriched, tail, context)
  //      }
  //
  //    def enrich(statement: CreateTable, context: Option[KeyspaceName]): Option[DataDefinition] =
  //      statement.tableName.keyspace.orElse(context).map(setKeyspace(statement, _))
  //
  //    def setKeyspace(statement: CreateTable, keyspace: KeyspaceName): CreateTable =
  //      statement.copy(tableName = statement.tableName.copy(keyspace = Some(keyspace)))
  //
  //    traverse(Seq.empty, statements, None)
  //  }

}

//case class TypedQuery[Partition, Row](partitionFields: Partition, rowFields: Row, keyspace: String, table: String)