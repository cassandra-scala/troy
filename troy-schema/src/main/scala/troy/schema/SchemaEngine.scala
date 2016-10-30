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
import troy.cql.ast.dml.Update.UpdateOperator
import troy.cql.ast.dml._
import troy.schema.V.Success
import troy.schema.validation.Validations

trait SchemaEngine {
  import SchemaEngine._

  def apply(statement: DataManipulation): Result[(RowType, VariableTypes)]
  def +(statement: DataDefinition): Result[SchemaEngine]
}

case class SchemaEngineImpl(schema: Schema, context: Option[KeyspaceName]) extends SchemaEngine {
  import SchemaEngine._

  private val validations = Validations(schema)

  override def apply(input: DataManipulation) = {
    val statement = enrichWithContext(input)
    for {
      rowType <- extractRowType(statement)
      variableTypes <- extractVariableTypes(statement)
      _ <- validations.validate(statement)
    } yield (rowType, variableTypes)
  }

  private def extractRowType(query: DataManipulation): Result[RowType] = query match {
    case stmt: SelectStatement =>
      extractRowType(stmt)
    case stmt: InsertStatement =>
      extractRowType(stmt)
    case stmt: DeleteStatement =>
      extractRowType(stmt)
    case stmt: UpdateStatement =>
      extractRowType(stmt)
  }

  private def extractRowType(query: SelectStatement): Result[RowType] = query match {
    case SelectStatement(_, Select.Asterisk, table, _, _, _, _, _) =>
      schema.getColumns(table).map(cs => Asterisk(cs.map(_.dataType).toSet))
    case SelectStatement(_, Select.SelectClause(items), table, _, _, _, _, _) =>
      schema.getColumns(table, items.map(_.selector).map {
        case Select.ColumnName(name) => name
        case _                       => ???
      }).map(RowType.fromColumns)
  }

  private def extractRowType(query: InsertStatement): Result[RowType] =
    V.Success(Columns(
      if (query.ifNotExists)
        Seq(DataType.Boolean)
      else
        Seq.empty
    ))

  private def extractRowType(query: DeleteStatement): Result[RowType] =
    V.Success(Columns(
      if (query.ifCondition.isDefined)
        Seq(DataType.Boolean)
      else
        Seq.empty
    ))

  private def extractRowType(query: UpdateStatement): Result[RowType] =
    V.Success(Columns(
      if (query.ifCondition.isDefined)
        Seq(DataType.Boolean)
      else
        Seq.empty
    ))

  private def extractVariableTypes(statement: DataManipulation): Result[Seq[DataType]] = statement match {
    case SelectStatement(_, _, from, Some(where), _, _, _, _)     => extractVariableTypes(from, where)
    case SelectStatement(_, _, from, None, _, _, _, _)            => V.Success(Seq.empty)
    case InsertStatement(table, clause: Insert.NamesValues, _, _) => extractVariableTypes(table, clause)
    case InsertStatement(table, clause: Insert.JsonClause, _, _)  => V.Success(Seq.empty)
    case s: DeleteStatement                                       => extractVariableTypes(s)
    case s: UpdateStatement                                       => extractVariableTypes(s)
  }

  private def extractVariableTypes(tableName: TableName, where: WhereClause): Result[Seq[DataType]] =
    schema.getTable(tableName).flatMap { table => extractVariableTypes(table, where) }

  private def extractVariableTypes(table: Table, where: WhereClause): Result[Seq[DataType]] =
    V.merge(where.relations.map {
      case WhereClause.Relation.Simple(columnName, op, BindMarker.Anonymous) =>
        import ColumnOps.Operations
        table.getColumn(columnName).flatMap(_.operandType(op)).map(dt => Seq(dt))
      case WhereClause.Relation.Tupled(identifiers, _, _) => ???
      case WhereClause.Relation.Token(_, identifiers, _)  => ???
      case _                                              => noVariables
    }).map(_.flatten)

  private def extractVariableTypes(table: TableName, insertClause: Insert.NamesValues): Result[Seq[DataType]] = {
    val markedColumns = (insertClause.columnNames zip insertClause.values.values).collect {
      case (identifier, _: BindMarker) => identifier
    }
    schema.getColumns(table, markedColumns).map(_.map(_.dataType))
  }

  private def extractVariableTypes(s: DeleteStatement): Result[Seq[DataType]] =
    schema.getTable(s.from).flatMap { table =>
      V.merge(Seq(
        extractVariablesFromSimpleSelections(table, s.simpleSelection),
        extractVariablesFromUpdateParam(table, s.using),
        extractVariableTypes(table, s.where),
        extractVariablesFromIfCondition(table, s.ifCondition)
      )).map(_.flatten)
    }

  private def extractVariableTypes(s: UpdateStatement): Result[Seq[DataType]] =
    schema.getTable(s.tableName).flatMap { table =>
      V.merge(Seq(
        extractVariablesFromUpdateParam(table, s.using),
        extractVariablesFomSet(table, s.set),
        extractVariableTypes(table, s.where),
        extractVariablesFromIfCondition(table, s.ifCondition)
      )).map(_.flatten)
    }

  private def extractVariablesFomSet(table: Table, set: Seq[Update.Assignment]): Result[Seq[DataType]] =
    V.merge(set.map {
      case Update.SimpleSelectionAssignment(selection, term: BindMarker) => //TODO: use extractVariablesFromTerm
        extractVariablesFromSimpleSelection(table, selection).map(x => Seq(x))
      case Update.TermAssignment(columnName1, columnName2, updateOperator, term) =>
        if (columnName1 == columnName2)
          extractVariablesFromTermAssigment(table, columnName2, updateOperator, term)
        else
          V.error(Messages.TermAssignmentSyntaxError)
      case Update.ListLiteralAssignment(columnName1, literalOrBindMarker, columnName2) =>
        if (columnName1 == columnName2)
          extractVariablesFromEitherListLiteralOrBindMarker(table, columnName1, literalOrBindMarker)
        else
          V.error(Messages.ListLiteralAssignmentSyntaxError)
      case _ => noVariables
    }).map(_.flatten)

  private def extractVariablesFromEitherListLiteralOrBindMarker(table: Table, columnName: Identifier, literalOrBindMarker: Either[ListLiteral, BindMarker]): Result[Seq[DataType]] = {
    literalOrBindMarker match {
      // TODO: validate that column is actually a list
      case Left(listLiteral) => extractVariablesFromListLiteralAssignment(table, columnName, listLiteral)
      case Right(bindMarker) => table.getColumn(columnName).map(_.dataType).flatMap {
        case DataType.List(t) => V.success(Seq(DataType.List(t)))
        case other            => V.error(Messages.ListLiteralAssignmentFailure(columnName, other))
      }
    }
  }

  private def extractVariablesFromTermAssigment(table: Table, columnName: Identifier, updateOperator: Update.UpdateOperator, term: Term): Result[Seq[DataType]] =
    table.getColumn(columnName).map(_.dataType).flatMap {
      case DataType.List(t)   => extractVariablesFromTerm(term, DataType.List(t))
      case DataType.Map(k, t) => extractVariablesFromTerm(term, DataType.Map(k, t))
      case DataType.Set(t)    => extractVariablesFromTerm(term, DataType.Set(t))
      case DataType.Counter   => extractVariablesFromTerm(term, DataType.Int)
      case other              => V.error(Messages.TermAssignmentFailure(columnName, term))
    }

  private def extractVariablesFromListLiteralAssignment(table: Table, columnName: Identifier, term: Term): Result[Seq[DataType]] =
    table.getColumn(columnName).map(_.dataType).flatMap {
      case DataType.List(t) => extractVariablesFromTerm(term, DataType.List(t))
      case other            => V.error(Messages.ListLiteralAssignmentFailure(columnName, other))
    }

  private def extractVariablesFromSimpleSelections(table: Table, selections: Seq[SimpleSelection]): Result[Seq[DataType]] =
    V.merge(selections.map(extractVariablesFromSimpleSelectionColumnNameOf(table, _))).map(_.flatten)

  private def extractVariablesFromSimpleSelectionColumnNameOf(table: Table, selection: SimpleSelection): Result[Seq[DataType]] =
    selection match {
      case SimpleSelection.ColumnNameOf(identifier, _: BindMarker) =>
        table.getColumn(identifier).map(_.dataType).map {
          case DataType.List(_)   => Seq(DataType.Int)
          case DataType.Map(k, _) => Seq(k)
        }
      case _ => noVariables
    }

  private def extractVariablesFromSimpleSelection(table: Table, selection: SimpleSelection): Result[DataType] = {
    selection match {
      case SimpleSelection.ColumnName(identifier)                  => table.getColumn(identifier).map(_.dataType)
      case SimpleSelection.ColumnNameOf(identifier, _: BindMarker) => extractVariablesFromSimpleSelectionColumnNameOf(table, selection).map(_.head)
      case SimpleSelection.ColumnNameDot(identifier, _)            => table.getColumn(identifier).map(_.dataType)
    }
  }

  private def extractVariablesFromUpdateParamValue(value: UpdateParamValue): Option[DataType] =
    value match {
      case UpdateVariable(_) => Some(DataType.Int)
      case UpdateValue(_)    => None
    }

  private def extractVariablesFromUpdateParam(table: Table, updateParam: Seq[UpdateParam]): Result[Seq[DataType]] =
    V.success(updateParam.flatMap {
      case Ttl(value: UpdateParamValue)       => extractVariablesFromUpdateParamValue(value)
      case Timestamp(value: UpdateParamValue) => extractVariablesFromUpdateParamValue(value)
    })

  private def extractVariablesFromTerm(term: Term, termType: DataType): Result[Seq[DataType]] =
    term match {
      case BindMarker.Anonymous => V.success(Seq(termType))
      case BindMarker.Named(_)  => V.success(Seq(termType))
      case _                    => noVariables
    }

  private def getExpectedTermTypeInCondition(table: Table, selection: SimpleSelection, operator: Operator): Result[DataType] = {
    val selectionDataType = extractVariablesFromSimpleSelection(table, selection)

    operator match {
      case Operator.In => selectionDataType.map(dt => DataType.Tuple(Seq(dt)))
      case Operator.Contains =>
        selectionDataType map {
          case DataType.List(t)   => t
          case DataType.Set(t)    => t
          case DataType.Map(k, _) => k
          case _                  => ???
        }
      case Operator.ContainsKey => V.success(DataType.Text)
      case _                    => selectionDataType
    }
  }

  private def extractVariablesFromCondition(table: Table, condition: Condition): Result[Seq[DataType]] =
    condition match {
      case Condition(selection: SimpleSelection, operator: Operator, term: Term) => {
        V.merge(Seq(
          extractVariablesFromSimpleSelectionColumnNameOf(table, selection),
          getExpectedTermTypeInCondition(table, selection, operator).flatMap(extractVariablesFromTerm(term, _))
        )).map(_.flatten)
      }
    }

  private def extractVariablesFromIfCondition(table: Table, ifCondition: Option[IfExistsOrCondition]): Result[Seq[DataType]] =
    V.merge(ifCondition.map {
      case IfCondition(conditions: Seq[Condition]) => conditions.map(extractVariablesFromCondition(table, _))
      case IfExist                                 => Seq(noVariables)
    }.getOrElse(Seq(noVariables))).map(_.flatten)

  private val noVariables: Result[Seq[DataType]] = V.success(Seq.empty)

  override def +(stmt: DataDefinition) =
    validations.validate(stmt).map(_ => stmt) flatMap {
      case s: CreateKeyspace => schema.apply(s).map(s => copy(s))
      case s: CreateTable    => schema.apply(enrichWithContext(s)).map(s => copy(s))
      case s: CreateIndex    => schema.apply(enrichWithContext(s)).map(s => copy(s))
      case s: AlterTable     => schema.apply(s).map(s => copy(s))
      case UseStatement(kn)  => V.success(copy(context = Some(kn)))
    }

  private def enrichWithContext(tableName: TableName): TableName =
    tableName.copy(keyspace = tableName.keyspace.orElse(context))

  private def enrichWithContext(s: CreateTable): CreateTable =
    s.copy(tableName = enrichWithContext(s.tableName))

  private def enrichWithContext(s: CreateIndex): CreateIndex =
    s.copy(tableName = enrichWithContext(s.tableName))

  private def enrichWithContext(s: DataManipulation): DataManipulation =
    s match {
      case s: DeleteStatement => enrichWithContext(s)
      case s: InsertStatement => enrichWithContext(s)
      case s: SelectStatement => enrichWithContext(s)
      case s: UpdateStatement => enrichWithContext(s)
    }

  private def enrichWithContext(s: DeleteStatement): DeleteStatement =
    s.copy(from = enrichWithContext(s.from))

  private def enrichWithContext(s: InsertStatement): InsertStatement =
    s.copy(into = enrichWithContext(s.into))

  private def enrichWithContext(s: SelectStatement): SelectStatement =
    s.copy(from = enrichWithContext(s.from))

  private def enrichWithContext(s: UpdateStatement): UpdateStatement =
    s.copy(tableName = enrichWithContext(s.tableName))
}

object SchemaEngine {
  sealed trait RowType
  case class Asterisk(types: Set[DataType]) extends RowType
  case class Columns(types: Seq[DataType]) extends RowType
  object RowType {
    def fromColumns(columns: Seq[Column]): Columns =
      Columns(columns.map(_.dataType))
  }

  type VariableTypes = Seq[DataType]

  val empty: SchemaEngine =
    SchemaEngineImpl(Schema(Map.empty), None)

  def apply(statements: Seq[DataDefinition], previous: SchemaEngine = empty): Result[SchemaEngine] =
    statements.foldLeft[Result[SchemaEngine]](V.success(previous)) {
      case (schema, statement) =>
        schema.flatMap(_ + statement)
    }
}