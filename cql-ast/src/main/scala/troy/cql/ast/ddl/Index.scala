package troy
package cql.ast.ddl

import troy.cql.ast.MapLiteral

object Index {
  final case class Using(using: String, options: Option[MapLiteral])

  sealed trait IndexIdentifier {
    def columnName = this match {
      case Identifier(value) => value
      case Keys(of)          => of
    }
  }
  final case class Identifier(value: String) extends IndexIdentifier
  final case class Keys(of: String) extends IndexIdentifier
}
