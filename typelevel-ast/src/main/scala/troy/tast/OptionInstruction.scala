package troy.tast

sealed trait OptionInstruction
sealed trait IdentifierTOption[basicIdentifier <: Identifier, identifier <: Identifier] extends OptionInstruction
sealed trait ConstantTOption[basicIdentifier <: Identifier, constant <: Constant] extends OptionInstruction
sealed trait MapLiteralTOption[basicIdentifier <: Identifier, mapLiteral <: MapLiteral[_]] extends OptionInstruction
