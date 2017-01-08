package troy
package meta

import troy.cql.ast._
import troy.cql.ast.dml.Select._
import scala.collection.immutable._

import scala.meta._

object QueryUtils extends Utils {
  val imports = Seq(
    q"import troy.driver.query.select._",
    q"import Select._"
  )
  def constructQuery(inputType: Type, outputType: Type, rawQuery: String): Tree = {
    val xs = imports :+ translateQuery(inputType, outputType, getOrAbort(CqlParser.parseDML(rawQuery)), rawQuery)
    q"{..$xs}"
  }

  def translateQuery(inputType: Type, outputType: Type, query: DataManipulation, raw: String): Stat = query match {
    case SelectStatement(mod, selection, from, where, orderBy, perPartitionLimit, limit, allowFiltering) =>
      val ts: Seq[Type] = Seq(Type.Name("1"), translateKeyspace(from.keyspace), literal(from.table), translateSelection(selection), inputType, outputType)
      q"select[..$ts]($raw)"
  }

  def translateKeyspace(keyspace: Option[KeyspaceName]): Type =
    keyspace.map(k => t"${literal(k.name)}" ).getOrElse(t"NoKeyspace")

  def translateSelection(selection: Selection): Type = selection match {
    case Asterisk =>
      ???
    case SelectClause(items) =>
      foldAsTuple(items.map {
        case SelectionClauseItem(selector, None) =>
          translateSelector(selector)
        case SelectionClauseItem(selector, Some(alias)) =>
          ???
      })
  }

  def translateSelectors(selectors: Seq[Selector]): Type =
    foldAsTuple(selectors.map(translateSelector))

  def foldAsTuple(ts: Seq[Type]): Type = t"(..$ts)"

  //  def foldAsHList(ts: Seq[Type]): Type = ts.reverse.foldLeft[Type](t"HNil") {
//    case (acc: Type, s: Type) =>
//      t"$s :: $acc"
//  }

  def translateSelector(selector: Selector): Type = selector match {
    case ColumnName(name)       => t"Column[${literal(name)}]"
    case SelectTerm(term)       => ???
    case Cast(selector, typ)    => ???
    case troy.cql.ast.dml.Select.Function(FunctionName(keyspace, name), params) =>
      t"Function[${translateKeyspace(keyspace)}, ${literal(name)}, ${translateSelectors(params)}]"
    case Count                  => ???
  }
}

class schemasafe extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    def transform(t: Tree) =
      defn.transform {
        case q"query[$t1, $t2](${p: Lit})" =>
          QueryUtils.constructQuery(t1, t2, p.value.toString)
      }

    defn match {
      case tree: Tree =>
        val q = transform(tree)
        println(q)
        q
      case _ =>
        abort(???, "")
    }
  }
}