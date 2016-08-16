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
import troy.cql.ast.dml.SelectStatement.WhereClause.Relation
import troy.cql.ast.dml._

import scala.util.Left

trait Schema {
  import Schema._

  def apply(statement: DataManipulation): Result[(RowType, VariableTypes)]
  def +(statement: DataDefinition): Result[Schema]
}

case class SchemaImpl(schema: Map[KeyspaceName, Map[CreateTable, Seq[CreateIndex]]], context: Option[KeyspaceName]) extends Schema {
  import Schema._

  override def apply(statement: DataManipulation) =
    for {
      rowType <- extractRowType(statement).right
      variableTypes <- extractVariableTypes(statement).right
      _ <- validate(statement).right
    } yield (rowType, variableTypes)

  private def extractRowType(query: DataManipulation): Result[RowType] = query match {
    case stmt: SelectStatement =>
      extractRowType(stmt)
    case stmt: InsertStatement =>
      extractRowType(stmt)
    case _ =>
      success(Columns(Seq.empty)) // TODO: Statements with If Not exists should return a row with a single boolean [applied flat]
  }

  private def validate(query: DataManipulation): Result[Unit] = query match {
    case stmt: SelectStatement =>
      validate(stmt)
    case _ =>
      success(())
  }

  private def extractRowType(query: SelectStatement): Result[RowType] = query match {
    case SelectStatement(_, SelectStatement.Asterisk, table, _, _, _, _, _) =>
      getAllColumns(table).right.map(cs => Asterisk(cs.map(_.dataType)))
    case SelectStatement(_, SelectStatement.SelectClause(items), table, _, _, _, _, _) =>
      apply(table, items.map(_.selector).map {
        case SelectStatement.ColumnName(name) => name
        case _                                => ???
      }).right.map(RowType.fromColumns)
  }

  private def extractRowType(query: InsertStatement): Result[RowType] =
    success(Columns(
      if (query.ifNotExists)
        Seq(DataType.boolean)
      else
        Seq.empty
    ))

  private def validate(query: SelectStatement): Result[Unit] = query match {
    case SelectStatement(_, _, _, _, _, _, _, true) =>
      success(())
    case SelectStatement(_, _, table, Some(where), _, _, _, false) =>
      val columnNames: Seq[Identifier] = where.relations.flatMap {
        case Relation.Simple(columnName, _, _) => Seq(columnName.name)
        case Relation.Tupled(columnNames, _, _) => columnNames.map(_.name)
        case Relation.Token(columnNames, _, _) => columnNames.map(_.name)
      }

      //      val relations = where.relations
      //      relations.match {
      //        case Relation.Simple(columnName, _, _) =>
      //          columnName.name.matches {
      //            case pkey
      //            case indx
      //            case clastercol => check next relation
      //          }
      //      }

      ???
    case _ =>
      success(())
  }
  private def extractVariableTypes(statement: DataManipulation): Result[Seq[DataType]] = statement match {
    case SelectStatement(_, _, from, Some(where), _, _, _, _)              => extractVariableTypes(from, where)
    case SelectStatement(_, _, from, None, _, _, _, _)                     => success(Seq.empty)
    case InsertStatement(table, clause: InsertStatement.NamesValues, _, _) => extractVariableTypes(table, clause)
    case InsertStatement(table, clause: InsertStatement.JsonClause, _, _)  => success(Seq.empty)
    case _                                                                 => ???
  }

  private def extractVariableTypes(table: TableName, where: SelectStatement.WhereClause): Result[Seq[DataType]] =
    for {
      table <- getTable(table).right
      dts <- extractVariableTypes(table, where).right
    } yield dts

  private def extractVariableTypes(table: CreateTable, where: SelectStatement.WhereClause): Result[Seq[DataType]] =
    Result.flattenSeq(where.relations.map {
      case SelectStatement.WhereClause.Relation.Simple(columnName, op, BindMarker.Anonymous) =>
        import ColumnOps.Operations
        for {
          column <- getColumn(table, columnName).right
          dt <- column.operandType(op).toRight(s"Operator $op doesn't support column type ${column.dataType}").right
        } yield Seq(dt)
      case SelectStatement.WhereClause.Relation.Tupled(identifiers, _, _) => ???
      case SelectStatement.WhereClause.Relation.Token(_, identifiers, _)  => ???
    })

  private def extractVariableTypes(table: TableName, insertClause: InsertStatement.NamesValues): Result[Seq[DataType]] =
    for {
      table <- getTable(table).right
      bindableColumns <- getColumns(
        table,
        (insertClause.columnNames zip insertClause.values.values).collect {
          case (identifier, _: BindMarker) => identifier
        }
      ).right
    } yield bindableColumns.map(_.dataType)

  private def apply(table: TableName, columns: Seq[String]): Result[Seq[CreateTable.Column]] =
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

  private def getTable(fullTableName: TableName): Result[CreateTable] =
    getTable(fullTableName.keyspace, fullTableName.table)

  private def getTable(keyspaceName: Option[KeyspaceName], tableName: String): Result[CreateTable] =
    for {
      tables <- getKeyspace(keyspaceName).right
      table <- getTable(tables, tableName).right
    } yield table

  private def getColumn(table: CreateTable, columnName: SelectStatement.ColumnName): Result[CreateTable.Column] =
    getColumn(table, columnName.name)

  private def getColumn(table: CreateTable, columnName: String): Result[CreateTable.Column] =
    table.columns.find(_.name == columnName).toRight(s"Column $columnName not found")

  private def getColumns(table: CreateTable, columnNames: Seq[String]): Result[Seq[CreateTable.Column]] =
    Result.seq(columnNames.map(getColumn(table, _)))

  def getColumns(keyspaceName: Option[KeyspaceName], table: String, selectColumns: Seq[String]): Result[Seq[CreateTable.Column]] =
    for {
      table <- getTable(keyspaceName, table).right
      columns <- getColumns(table, selectColumns).right
    } yield columns

  private def getAllColumns(table: TableName): Result[Set[CreateTable.Column]] =
    getAllColumns(table.keyspace, table.table)

  private def getAllColumns(keyspaceName: Option[KeyspaceName], table: String): Result[Set[CreateTable.Column]] =
    for {
      table <- getTable(keyspaceName, table).right
    } yield table.columns.toSet

  override def +(stmt: DataDefinition) = stmt match {
    case s: CreateKeyspace => withKeyspace(s)
    case s: CreateTable    => withTable(s)
    case s: CreateIndex    => success(this) // Indexes are ignored for now. TODO: https://github.com/tabdulradi/troy/issues/36
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

  def withTable(createTable: CreateTable) =
    for {
      keyspace <- resolveKeyspaceName(createTable.tableName.keyspace).right
      tables <- getKeyspace(keyspace).right
      _ <- (if (tableExists(tables, createTable)) fail(s"Table ${createTable.tableName} already exists") else success).right
    } yield copy(schema = schema + (keyspace -> (createTable +: tables)))

  def withIndex(createIndex: CreateIndex) =
}

object Schema {
  type Result[T] = Either[String, T]

  sealed trait RowType
  case class Asterisk(types: Set[DataType]) extends RowType
  case class Columns(types: Seq[DataType]) extends RowType
  object RowType {
    def fromColumns(columns: Seq[CreateTable.Column]): Columns =
      Columns(columns.map(_.dataType))
  }

  type VariableTypes = Seq[DataType]

  object Result {
    def seq[T](results: Seq[Result[T]]): Result[Seq[T]] =
      results.collectFirst {
        case Left(e) => fail(e)
      }.getOrElse(success(results.map(_.right.get)))

    def flattenSeq[T](results: Seq[Result[Seq[T]]]): Result[Seq[T]] =
      for {
        s <- seq(results).right
      } yield s.flatten
  }

  private val empty: Schema = SchemaImpl(Map.empty, None)

  def apply(statements: Seq[DataDefinition]) =
    (success(empty) /: statements) {
      case (Right(schema), statement) => schema + statement
      case (Left(e), _)               => fail(e)
    }

  def fail[T](msg: String): Result[T] =
    Left(msg)

  def success[T](result: T): Result[T] =
    Right(result)

  def success(): Result[_] =
    Right(())
}