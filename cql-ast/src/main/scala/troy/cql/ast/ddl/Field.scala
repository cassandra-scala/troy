package troy.cql.ast.ddl

import troy.cql.ast.{ DataType, Identifier }

final case class Field(fieldName: Identifier, fieldType: DataType)
