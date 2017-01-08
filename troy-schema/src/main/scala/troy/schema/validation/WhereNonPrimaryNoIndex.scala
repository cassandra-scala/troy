package troy
package schema.validation

import troy.cql.ast._
import troy.cql.ast.dml.WhereClause
import troy.schema.Schema

class WhereNonPrimaryNoIndex(schema: Schema) extends Validation[DataManipulation] {
  override def rules = {
    case SelectStatement(_, _, from, Some(where), _, _, _, false) =>
      noMessages // TODO
  }

  def validateWhereClause(tableName: TableName, where: WhereClause) =
    schema.getColumns(tableName, columnNamesInRelations(where.relations))

  def columnNamesInRelations(relations: Seq[WhereClause.Relation]) =
    relations.flatMap {
      case WhereClause.Relation.Simple(columnName, _, _) =>
        Seq(columnName)
      case WhereClause.Relation.Tupled(columnNames, _, _) =>
        columnNames
      case WhereClause.Relation.Token(columnNames, _, _) =>
        columnNames
    }
}
