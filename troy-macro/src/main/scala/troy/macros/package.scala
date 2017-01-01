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

import scala.reflect.macros.blackbox.Context
import troy.cql.ast.{DataType, KeyspaceName, TableName}
import troy.schema._

package object macros {
  import CqlOps._

  private val loadedSchema = new ResourceFileOrFolderSchemaLoader("/schema.cql", "/schema/").load

  def log[T](value: T): T = { println(value); value }

  def troyImpl[F](c: Context)(code: c.Expr[F]): c.Expr[F] = {
    import c.universe._
    implicit val c_ = c

    val queryConfig = readQueryConfig(c)()

    val q"(..$params) => $exprWithDsl" = code.tree
    val expr = removeMacroDslClasses(c)(exprWithDsl)

    val (qParts, qParams) = findCqlQuery(c)(expr)
    val rawQuery = qParts.map{case q"${p: String}" => p}.mkString("?")
    val schema = getOrAbort(loadedSchema)

    val versionsSchemaImplicits = for {
      (v, schema: SchemaEngineImpl) <- schema.asInstanceOf[VersionedSchemaEngineImpl].schemas
      identifier = TermName(s"v${v}Exists")
    } yield q"implicit val $identifier = VersionExists.instance[$v]"

    val keyspacesSchemaImplicits = for {
      (v, schema: SchemaEngineImpl) <- schema.asInstanceOf[VersionedSchemaEngineImpl].schemas
      (KeyspaceName(k), keyspace) <- schema.schema.keyspaces
      identifier = TermName(s"keyspace${k}ExistsInV$v")
    } yield q"implicit val $identifier = KeyspaceExists.instance[$v, $k]"

    val tablesSchemaImplicits = for {
      (v, schema: SchemaEngineImpl) <- schema.asInstanceOf[VersionedSchemaEngineImpl].schemas
      (KeyspaceName(k), keyspace) <- schema.schema.keyspaces
      (TableName(_, t), table) <- keyspace.tables
      identifier = TermName(s"table${t}ExistsInKeyspace${k}InV$v")
    } yield q"implicit val $identifier = TableExists.instance[$v, $k, $t]"

    val columnsSchemaImplicits = for {
      (v, schema: SchemaEngineImpl) <- schema.asInstanceOf[VersionedSchemaEngineImpl].schemas
      (KeyspaceName(k), keyspace) <- schema.schema.keyspaces
      (TableName(_, t), table) <- keyspace.tables
      (cName, column) <- table.columns
      ct = translateColumnType(c)(column.dataType)
      identifier = TermName(s"column${cName}ExistsInTable${t}InKeyspace${k}InV$v")
    } yield q"implicit val $identifier = ColumnType.instance[$v, $k, $t, $cName, $ct]"


    val query = getOrAbort(parseQuery(rawQuery))
    val (rowType, variableDataTypes) = getOrAbort {
      queryConfig match {
        case QueryConfig(None, None) => schema(query)
        case QueryConfig(Some(min), Some(max)) => schema(query, min, max)
        case QueryConfig(Some(min), None) => schema(query, min)
      }
    }

    val imports = Seq(
      q"import _root_.troy.driver.InternalDsl._",
      q"import _root_.troy.driver.codecs.PrimitivesCodecs._",
      q"import troy.driver.schema.version.VersionExists",
      q"import troy.driver.schema.keyspace.KeyspaceExists",
      q"import troy.driver.schema.table.TableExists",
      q"import troy.driver.schema.column.ColumnType"
    ) ++ versionsSchemaImplicits ++ keyspacesSchemaImplicits ++ tablesSchemaImplicits ++ columnsSchemaImplicits

    val session = q"implicitly[com.datastax.driver.core.Session]"

    val prepareStatement = q"""
      val prepared = $session.prepare($rawQuery)
    """

    val parser = expr match {
      case q"$root.as[..$paramTypes]($f)" =>
        val columnTypes = translateColumnTypes(c)(rowType match {
          case SchemaEngine.Asterisk(_) => c.abort(c.enclosingPosition, "Troy doesn't support using .as with Select * queries")
          case SchemaEngine.Columns(types) => types
        })
        val params = (paramTypes zip columnTypes).zipWithIndex.map {
          case ((p, c), i) =>
            q"column[$p]($i)(row).as[$c]"
        }
        q"def parser(row: _root_.com.datastax.driver.core.Row) = $f(..$params)"
      case _ =>
        q"" // Parser is ignored if ".as(...)" was omitted.
    }


    val body = {
      val translatedVariableTypes = translateColumnTypes(c)(variableDataTypes)
      val bodyParams = qParams.zip(translatedVariableTypes).map{ case (p, t) => q"param($p).as[$t]" }
      replaceCqlQuery(c)(expr, q"bind(prepared, ..$bodyParams)") match {
        case q"$root.as[..$paramTypes]($f)" => q"$root.parseAs(parser)"
        case other => other
      }
    }

    val fullBody = Seq(parser, body)

    val stats = imports ++ Seq(
      prepareStatement,
      q"(..$params) => {..$fullBody}"
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

  case class QueryConfig(minVersion: Option[Int] = None, maxVersion: Option[Int] = None)

  private def readQueryConfig(c: Context)(tree: c.universe.Tree = c.prefix.tree, qc: QueryConfig = QueryConfig()): QueryConfig = {
    import c.universe._
    tree match {
      case q"$expr.minVersion(${v: Int})" =>
        readQueryConfig(c)(expr, qc.copy(minVersion = Some(v)))
      case q"$expr.maxVersion(${v: Int})" =>
        readQueryConfig(c)(expr, qc.copy(maxVersion = Some(v)))
      case q"$_.withSchema" =>
        qc
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

  private def translateColumnTypes(c: Context)(types: Seq[DataType]) = {
    types.map(t => translateColumnType(c)(t))
  }

  private def translateColumnType(c: Context)(t: DataType) = {
    t match {
      case t: DataType.Native => translateNativeColumnType(c)(t)
      case t: DataType.Collection => translateCollectionColumnType(c)(t)
    }
  }

  private def translateCollectionColumnType(c: Context)(typ: DataType): c.universe.Tree = {
    import c.universe._
    val cdt = q"CDT"
    def translate(t: DataType) = translateNativeColumnType(c)(t)
    typ match {
      case DataType.List(t) => tq"$cdt.List[${translate(t)}]"
      case DataType.Set(t) => tq"$cdt.Set[${translate(t)}]"
      case DataType.Map(k, v) => tq"$cdt.Map[${translate(k)}, ${translate(v)}]"
      //      case DataType.Tuple(ts: Seq[DataType]) => tq"$cdt."
      //      case DataType.Custom(javaClass: String) => tq"$cdt."
    }
  }

  private def translateNativeColumnType(c: Context)(typ: DataType): c.universe.Tree = {
    import c.universe._
    val cdt = q"CDT"
    typ match {
      case DataType.Ascii => tq"$cdt.Ascii"
      case DataType.BigInt => tq"$cdt.BigInt"
      case DataType.Blob => tq"$cdt.Blob"
      case DataType.Boolean => tq"$cdt.Boolean"
      case DataType.Counter => tq"$cdt.Counter"
      case DataType.Date => tq"$cdt.Date"
      case DataType.Decimal => tq"$cdt.Decimal"
      case DataType.Double => tq"$cdt.Double"
      case DataType.Float => tq"$cdt.Float"
      case DataType.Inet => tq"$cdt.Inet"
      case DataType.Int => tq"$cdt.Int"
      case DataType.Smallint => tq"$cdt.SmallInt"
      case DataType.Text => tq"$cdt.Text"
      case DataType.Time => tq"$cdt.Time"
      case DataType.Timestamp => tq"$cdt.Timestamp"
      case DataType.Timeuuid => tq"$cdt.TimeUuid"
      case DataType.Tinyint => tq"$cdt.TinyInt"
      case DataType.Uuid => tq"$cdt.Uuid"
      case DataType.Varchar => tq"$cdt.VarChar"
      case DataType.Varint => tq"$cdt.VarInt"
    }
  }

  def getOrAbort[T](res: Result[T])(implicit c: Context) =
    res match {
      case V.Success(data, warns) =>
        warns.foreach(w => c.warning(c.enclosingPosition, w.message))
        data
      case V.Error(errors, warns) =>
        warns.foreach(w => c.warning(c.enclosingPosition, w.message))
        errors.drop(1).foreach(e => c.error(c.enclosingPosition, e.message))
        c.abort(c.enclosingPosition, errors.head.message)
    }
}
