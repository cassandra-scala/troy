package troy
package schema.validation

import troy.schema.Messages.SelectedDistinctNonStaticColumn
import troy.schema.Schema
import troy.cql.ast._
import troy.cql.ast.dml.Select._

class SelectDistinctNonStaticColumns(schema: Schema) extends Validation[DataManipulation] {
  override def rules = {
    case SelectStatement(Some(Distinct), selection, from, _, _, _, _, _) =>
      columnsInSelection(from, selection).map { selectedColumns =>
        selectedColumns
          .filterNot(_.isPartitionLevel)
          .map(_.name)
          .map(SelectedDistinctNonStaticColumn)
      }
  }

  def columnsInSelection(table: TableName, selection: Selection) = selection match {
    case Asterisk            => schema.getColumns(table)
    case SelectClause(items) => schema.getColumns(table, items.map(_.selector).flatMap(columnNamesInSelector))
  }

  val columnNamesInSelector: Selector => Seq[Identifier] = {
    case ColumnName(name)       => Seq(name)
    case SelectTerm(_)          => Seq.empty
    case Cast(selector, _)      => columnNamesInSelector(selector)
    case Function(_, selectors) => selectors.flatMap(columnNamesInSelector)
  }
}
