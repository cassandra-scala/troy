package troy.meta

import troy.cql.ast.CqlParser
import troy.schema.{ Message, Result, V }

import scala.meta.Type

trait Utils {
  def abort(str: String): Nothing = {
    println(str)
    scala.meta.abort(str)
  }

  def getOrAbort[T](result: CqlParser.ParseResult[T]): T =
    result.getOrElse(abort("Parse error"))

  def warn(msg: String): Unit = println(msg)

  def warn(msg: Message): Unit = warn(msg.message)

  def getOrAbort[T](result: Result[T]): T =
    result match {
      case V.Success(value, ws) =>
        ws.foreach(warn)
        value
      case V.Error(es, ws) =>
        ws.foreach(warn)
        abort(es.head.message)
    }

  def quoted(str: String): String = s"""\"$str\""""

  def literal(str: String): Type.Name = Type.Name(quoted(str))

  def literal(num: Int): Type.Name = Type.Name(num.toString)
}
