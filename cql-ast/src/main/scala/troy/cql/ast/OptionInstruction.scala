package troy
package cql.ast

sealed trait OptionInstruction

final case class IdentifierOption(basicIdentifier: Identifier, identifier: Identifier) extends OptionInstruction
final case class ConstantOption(basicIdentifier: Identifier, constant: Constant) extends OptionInstruction
final case class MapLiteralOption(basicIdentifier: Identifier, mapLiteral: MapLiteral) extends OptionInstruction
