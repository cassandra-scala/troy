package troy.tast

import troy.tutils.TOption

object Insert {
  sealed trait InsertClause
  sealed trait NamesValues[columnNames <: THList[Identifier], values <: TupleLiteral[_]] extends InsertClause
  sealed trait JsonClause[value <: String, default <: TOption[Default]] extends InsertClause

  sealed trait Default
  sealed trait NullValue extends Default
  sealed trait Unset extends Default
}