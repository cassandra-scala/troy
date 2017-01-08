package troy
package cql.ast.dml

import troy.cql.ast.BindMarker

sealed trait UpdateParam
final case class Timestamp(value: UpdateParamValue) extends UpdateParam
final case class Ttl(value: UpdateParamValue) extends UpdateParam

sealed trait UpdateParamValue
final case class UpdateValue(value: String) extends UpdateParamValue //TODO should be Int?
final case class UpdateVariable(bindMarker: BindMarker) extends UpdateParamValue