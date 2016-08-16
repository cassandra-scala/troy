package troy.cql.ast.dml

import troy.cql.ast.BindMarker

sealed trait UpdateParam
case class Timestamp(value: UpdateParamValue) extends UpdateParam
case class Ttl(value: UpdateParamValue) extends UpdateParam

sealed trait UpdateParamValue
case class UpdateValue(value: String) extends UpdateParamValue
case class UpdateVariable(bindMarker: BindMarker) extends UpdateParamValue