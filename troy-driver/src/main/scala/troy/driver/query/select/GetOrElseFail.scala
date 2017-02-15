package troy.driver.query.select

import shapeless._
import troy.driver.CassandraDataType
import troy.driver.schema.FunctionTypeResolver
import troy.driver.schema.column.ColumnType
import troy.tast.Select.{ Selection, ColumnName, Function, SelectClause, SelectionClauseItem }
import troy.tutils._
import scala.reflect.macros.blackbox.Context

trait GetOrElseFail[M] {
  type Out
}

object GetOrElseFail {

  type Aux[M, O] = GetOrElseFail[M] { type Out = O }

  def apply[M](implicit instance: GetOrElseFail[M]): Aux[M, instance.Out] = instance

  def instance[M, O]: Aux[M, O] = new GetOrElseFail[M] { override type Out = O }

  implicit def getEitherIfRight[T] = instance[TRight[T], T]
  implicit def failEitherIfLeft[E <: THList[String]]: Aux[TLeft[E], Nothing] = macro GetOrElseFailMacro.fail[E]
}

object GetOrElseFailMacro {
  def fail[E <: THList[String]](c: Context)(implicit eType: c.WeakTypeTag[E]) = {
    c.abort(c.enclosingPosition, show(c)(eType.tpe))
  }

  def show(c: Context)(typ: c.universe.Type): String = typ match {
    case t if t <:< c.typeOf[TNone]              => ""
    case t if t <:< c.typeOf[HNil]               => ""
    case t if t <:< c.typeOf[TSome[_]]           => show(c)(typ.typeArgs.head)
    case t if t <:< c.typeOf[shapeless.::[_, _]] => typ.typeArgs.map(show(c)).mkString
    case t                                       => trim(t.toString)
  }

  def trim(typRepr: String): String = typRepr match {
    case StringPattern(str) => str
    case other              => other
  }

  val StringPattern = """String\("(.*)"\)""".r

}