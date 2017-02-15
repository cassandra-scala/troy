package troy
package meta

import troy.cql.ast.CqlParser
import troy.schema.{ Message, Result, V }

import scala.meta._
import scala.reflect.ClassTag

trait Utils {
  def abort(str: String): Nothing = {
    println(str)
    scala.meta.abort(str)
  }

  def notImplemented(msg: String) = {
    abort("Not implemented " + msg)
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

  private def composeType[T](params: Type*)(implicit classTag: ClassTag[T]): Type = {
    val t = Type.Name(classTag.runtimeClass.getName.replace("$", ".").replaceFirst("troy.tast.", "").replaceFirst("troy.tutils.", ""))
    t"$t[..${params.to[scala.collection.immutable.Seq]}]"
  }

  def t[T](implicit classTag: ClassTag[T]): Type =
    Type.Name(classTag.runtimeClass.getName.replace("$", ".").replaceFirst("troy.tast.", "").replaceFirst("troy.tutils.", ""))

  def t[T: ClassTag](params: Type*): Type =
    composeType[T](params: _*)

  //  class t[T] {
  //    def apply[U[_]](p1: Type)(implicit classTag: ClassTag[T], ev: T <:< U[_]): Type =
  //      composeType[T](p1)
  //
  //    def apply[U[_, _]](p1: Type, p2: Type)(implicit classTag: ClassTag[T], ev: T <:< U[_, _]): Type =
  //      composeType[T](p1, p2)
  //
  //    def apply[U[_, _, _]](p1: Type, p2: Type, p3: Type)(implicit classTag: ClassTag[T], ev: T <:< U[_, _, _]): Type =
  //      composeType[T](p1, p2, p3)
  //  }
  //  object t {
  //    def apply[T] = new t[T]
  ////    implicit def xx[U: ClassTag](instance: t[U]): Type = composeType[U]()
  //  }
}
