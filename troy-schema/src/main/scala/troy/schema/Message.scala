package troy
package schema

import troy.cql.ast._
import troy.cql.ast.dml.Operator

/**
 * Expresses an issue, can be interpreted as warn or error based on configured levels
 */
sealed abstract class Message(val message: String) extends Product
object Messages {
  case object KeyspaceNotSpecified extends Message("Keyspace not specified")
  case class KeyspaceNotFound(k: KeyspaceName) extends Message(s"Keyspace '${k.name}' not found")
  case class TableNotFound(t: TableName) extends Message(s"Table '$t' not found")
  case class ColumnNotFound(c: Identifier, t: TableName) extends Message(s"Column '$c' not found in table '$t'")
  case class OperatorNotSupported(op: Operator, dt: DataType) extends Message(s"Operator '$op' doesn't support column type '$dt'")

  case class KeyspaceAlreadyExists(k: KeyspaceName) extends Message(s"Keyspace $k already exists")
  case class TableAlreadyExists(t: TableName) extends Message(s"Table $t already exists")
  case class IndexAlreadyExists(name: Option[String], index: Index, on: String) extends Message(s"Index ${name.getOrElse("")} of type $index already exists on $on")
  case class ColumnAlreadyExists(t: TableName, c: Identifier) extends Message(s"Invalid column name $c because it conflicts with an existing column in table $t")

  case class CannotDropPrimaryKeyPart(c: Identifier) extends Message(s"Cannot drop PRIMARY KEY part $c")
  case class IncompatibleAlterType(c: Identifier, ot: DataType, nt: DataType) extends Message(s"Cannot change $c from type $ot to type $nt: types are incompatible.")

  case class PrimaryKeyNotDefined(t: TableName) extends Message(s"CREATE TABLE $t statement has no Primary key defined")

  //TODO: Format Term
  case class TermAssignmentFailure(c: Identifier, v: Term) extends Message(s"Invalid operation ($c = $c + $v) for non counter column $c")
  case object TermAssignmentSyntaxError extends Message("Only expressions of the form X = X +<value> are supported.")
  case object ListLiteralAssignmentSyntaxError extends Message("Only expressions of the form X = <value> + X are supported.")
  case class ListLiteralAssignmentFailure(c: Identifier, nt: DataType) extends Message(s"Invalid list literal for $c of type $nt.")

  // Macro
  case class SchemaNotFound(path: String) extends Message(s"Can't find schema file $path")
  case class SchemaParseFailure(msg: String, line: Int, column: Int) extends Message(s"Failure during parsing the schema. Error ($msg) near line $line, column $column")
  case class QueryParseFailure(msg: String, line: Int, column: Int) extends Message(s"Failure during parsing query. Error ($msg) near line $line, column $column")

  // Validations
  case class SelectedDistinctNonStaticColumn(c: Identifier) extends Message(s"SELECT DISTINCT queries must only request partition key columns and/or static columns (not $c)")

  // Versioned
  case class QueryNotCrossCompatible(messages: Seq[(VersionedSchemaEngine.Version, Seq[Message], Seq[Message])]) extends Message(
    // TODO: Erros and Warns need to be seperated
    s"Query is not cross compatible.\n" + messages.map {
      case (v, es, ws) =>
        s"Version $v:\n" + es.map(_.message).map("  " + _).mkString("\n")
    }.mkString("\n")
  )
}
