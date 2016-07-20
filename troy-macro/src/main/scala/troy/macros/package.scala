/*
 * Copyright 2016 Tamer AbdulRadi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package troy

import java.io.InputStream
import scala.io.Source
import scala.reflect.api.Trees
import scala.reflect.macros.blackbox.Context
import com.datastax.driver.core.Session
import troy.cql.ast.CqlParser
import troy.cql.ast.DataType
import troy.schema.Schema

package object macros {

  def log[T](value: T): T = { println(value); value }

  def troyImpl[F](c: Context)(code: c.Expr[F]): c.Expr[F] = {
    import c.universe._
    implicit val c_ = c

    val q"(..$params) => $exprWithDsl" = code.tree
    val expr = removeMacroDslClasses(c)(exprWithDsl)

    val (qParts, qParams) = findCqlQuery(c)(expr)
    val rawQuery = qParts.map{case q"${p: String}" => p}.mkString("?")
    val schema = parseSchemaFromFileName("/schema.cql")(c)
    val query = parseQuery(rawQuery)

    val imports = Seq(
      q"import _root_.troy.dsl.InternalDsl._",
      q"import _root_.troy.driver.CassandraDataType",
      q"import _root_.troy.codecs.HasTypeCodec._",
      q"import _root_.troy.codecs.Primitives._"
    )

    val session = q"implicitly[com.datastax.driver.core.Session]"

    val prepareStatement = q"""
      val prepared = $session.prepare($rawQuery)
    """

    val parser = expr match {
      case q"$root.as[..$paramTypes]($f)" =>
        val columns = schema(query) match {
          case Right(columns) => columns
          case Left(e)        => c.abort(c.enclosingPosition, e)
        }
        val columnTypes = columns.map(column => translateColumnType(c)(column.dataType))
        val params = (paramTypes zip columnTypes).zipWithIndex.map {
          case ((p, c), i) =>
            q"column[$p]($i)(row).as[$c]"
        }
        q"def parser(row: Row) = $f(..$params)"
      case _ =>
        q"" // Parser is ignored if ".as(...)" was omitted.
    }


    val body = {
      val variable = schema.extractVariables(query) match {
        case Right(columns) => columns
        case Left(e)        => c.abort(c.enclosingPosition, e)
      }
      val variableTypes = variable.map(v => translateColumnType(c)(v.dataType))
      val bodyParams = qParams.zip(variableTypes).map{ case (p, t) => q"param($p).as[$t]" }
      replaceCqlQuery(c)(expr, q"bind(prepared, ..$bodyParams)") match {
        case q"$root.as[..$paramTypes]($f)" => q"$root.parseAs(parser)"
        case other => other
      }
    }

    val stats = imports ++ Seq(
      prepareStatement,
      parser,
      q"(..$params) => $body"
    ).filter(!_.isEmpty)

    c.Expr(log(q"{ ..$stats }"))
  }

  private def removeMacroDslClasses(c: Context)(expr: c.universe.Tree): c.universe.Tree = {
    import c.universe._
    replaceTree(c)(expr) {
      case q"troy.dsl.`package`.MacroDsl_RichStatement($param)" =>
        q"${removeMacroDslClasses(c)(param)}"
      case q"troy.dsl.`package`.MacroDsl_RichFutureBoundStatement($param)" =>
        q"${removeMacroDslClasses(c)(param)}"
      case q"troy.dsl.`package`.MacroDsl_RichResultSet($param)" =>
        q"${removeMacroDslClasses(c)(param)}"
      case q"troy.dsl.`package`.MacroDsl_RichFutureOfResultSet($param)" =>
        q"${removeMacroDslClasses(c)(param)}"
      case q"troy.dsl.`package`.MacroDsl_RichFutureOfSeqOfRow($param)" =>
        q"${removeMacroDslClasses(c)(param)}"
      case q"troy.dsl.`package`.MacroDsl_RichFutureOfOptionOfRow($param)" =>
        q"${removeMacroDslClasses(c)(param)}"
      case q"troy.dsl.`package`.MacroDsl_RichSeqOfRow($param)" =>
        q"${removeMacroDslClasses(c)(param)}"
      case q"troy.dsl.`package`.MacroDsl_RichOptionOfRow($param)" =>
        q"${removeMacroDslClasses(c)(param)}"
      case q"troy.driver.DSL.RichFutureOfResultSet($param)" =>
        q"${removeMacroDslClasses(c)(param)}"
      case q"troy.driver.DSL.RichResultSet($param)" =>
        q"${removeMacroDslClasses(c)(param)}"
      case q"troy.driver.DSL.ExternalDSL_RichStatement($param)" =>
        q"${removeMacroDslClasses(c)(param)}"
    }
  }

  private def findCqlQuery(c: Context)(expr: c.universe.Tree): (List[c.universe.Tree], List[c.universe.Tree]) = {
    import c.universe._
    expr match {
      case q"$_.RichStringContext(scala.StringContext.apply(..$query)).cql(..$params).prepared" =>
        (query, params)
      case q"$expr.$tname" =>
        findCqlQuery(c)(expr)
      case q"$expr.$func[..$tpts](...$exprss)"	=>
        findCqlQuery(c)(expr)
      case q"$expr.$func[..$tpts]" =>
        findCqlQuery(c)(expr)
    }
  }

  private def replaceCqlQuery(c: Context)(expr: c.universe.Tree, replacement: c.universe.Tree): c.universe.Tree = {
    import c.universe._
    expr match {
      case q"$_.RichStringContext(scala.StringContext.apply(..$query)).cql(..$params).prepared" =>
        replacement
      case q"$y(..$z)" =>
        val replaced = replaceCqlQuery(c)(y, replacement)
        q"$replaced(..$z)"
      case q"$y.$z" =>
        val replaced = replaceCqlQuery(c)(y, replacement)
        q"$replaced.$z"
      case q"$y[..$z]" =>
        val replaced = replaceCqlQuery(c)(y, replacement)
        q"$replaced[..$z]"
    }
  }

  private def replaceTree(c: Context)(original: c.universe.Tree)(handler: PartialFunction[c.universe.Tree, c.universe.Tree]): c.universe.Tree  = {
    import c.universe._
    def expand(input: c.universe.Tree): c.universe.Tree = {
      handler.orElse[c.universe.Tree, c.universe.Tree] {
        case q"$expr.$tname" =>
          q"${expand(expr)}.$tname" // Select
        case q"$expr.$func[..$tpts](...$exprss)"	=>
          val expandedExprss = exprss.map(e => e.map(x => expand(x)))
          q"${expand(expr)}.$func[..$tpts](...$expandedExprss)"
        case q"$expr.$func[..$tpts]" =>
          q"${expand(expr)}.$func[..$tpts]" // TypeApply
        case q"$func(..$params) = $body" =>
          q"$func(..$params) = ${expand(body)}" // Tree
        case other =>
          other
      }(input)
    }

    expand(original)
  }

  private def translateColumnType(c: Context)(typ: DataType): c.universe.Tree = {
    import c.universe._
    typ match {
      case t: DataType.Native => translateNativeColumnType(c)(t)
      case t: DataType.Collection => translateCollectionColumnType(c)(t)
    }
  }

  private def translateCollectionColumnType(c: Context)(typ: DataType): c.universe.Tree = {
    import c.universe._
    val cdt = q"CassandraDataType"
    def translate(t: DataType) = translateNativeColumnType(c)(t)
    typ match {
      case DataType.list(t) => tq"$cdt.List[${translate(t)}]"
      case DataType.set(t) => tq"$cdt.Set[${translate(t)}]"
      case DataType.map(k, v) => tq"$cdt.Map[${translate(k)}, ${translate(v)}]"
      //      case DataType.Tuple(ts: Seq[DataType]) => tq"$cdt."
      //      case DataType.Custom(javaClass: String) => tq"$cdt."
    }
  }

  private def translateNativeColumnType(c: Context)(typ: DataType): c.universe.Tree = {
    import c.universe._
    val cdt = q"CassandraDataType"
    typ match {
      case DataType.ascii => tq"$cdt.Ascii"
      case DataType.bigint => tq"$cdt.Bigint"
      case DataType.blob => tq"$cdt.Blob"
      case DataType.boolean => tq"$cdt.Boolean"
      case DataType.counter => tq"$cdt.Counter"
      case DataType.date => tq"$cdt.Date"
      case DataType.decimal => tq"$cdt.Decimal"
      case DataType.double => tq"$cdt.Double"
      case DataType.float => tq"$cdt.Float"
      case DataType.inet => tq"$cdt.Inet"
      case DataType.int => tq"$cdt.Int"
      case DataType.smallint => tq"$cdt.Smallint"
      case DataType.text => tq"$cdt.Text"
      case DataType.times => tq"$cdt.Times"
      case DataType.timestamp => tq"$cdt.Timestamp"
      case DataType.timeuuid => tq"$cdt.Timeuuid"
      case DataType.tinyint => tq"$cdt.Tinyint"
      case DataType.uuid => tq"$cdt.Uuid"
      case DataType.varchar => tq"$cdt.Varchar"
      case DataType.varint => tq"$cdt.Varint"
    }
  }


  def parseSchemaFromFileName(path: String)(implicit c: Context) =
    parseSchemaFromInputStream(
      Option(this.getClass.getResourceAsStream(path))
        .getOrElse(c.abort(c.universe.NoPosition, s"Can't find schema file $path"))
    )

  def parseSchemaFromInputStream(schemaFile: InputStream)(implicit c: Context) =
    parseSchemaFromSource(scala.io.Source.fromInputStream(schemaFile))

  def parseSchemaFromSource(schema: Source)(implicit c: Context) = {
    val lines = schema.getLines()
    val str = lines.mkString("\n")
    parseSchemaFromString(str)
  }

  def parseSchemaFromString(schema: String)(implicit c: Context) =
    CqlParser.parseSchema(schema) match {
      case CqlParser.Success(result, _) =>
        Schema(result) match {
          case Right(schema) => schema
          case Left(e)       => c.abort(c.enclosingPosition, e)
        }
      case CqlParser.Failure(msg, next) =>
        c.abort(c.universe.NoPosition, s"Failure during parsing the schema. Error ($msg) near line ${next.pos.line}, column ${next.pos.column}")
    }

  def parseQuery(queryString: String)(implicit c: Context) = CqlParser.parseQuery(queryString) match {
    case CqlParser.Success(result, _) =>
      result
    case CqlParser.Failure(msg, _) =>
      c.abort(c.enclosingPosition, msg)
  }

}
