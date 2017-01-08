package troy
package cql.ast.ddl

object Keyspace {
  sealed trait KeyspaceOption
  final case class Replication(options: Seq[(String, String)]) extends KeyspaceOption // TODO
}
