package troy
package meta

import troy.cql.ast._
import troy.cql.ast.dml.Operator
import troy.cql.ast.dml.Select._
import troy.tast.Select
import troy.tutils._

import scala.collection.immutable.{Seq => ImmutableSeq}
import troy.cql.ast.dml.WhereClause.Relation

import scala.meta._

object QueryUtils extends Utils {

  val imports = ImmutableSeq(
    q"import troy.driver.query.select.Select.Query",
    q"import troy.tast._",
    q"import troy.tutils._"
  )
  def constructQuery(inputType: Type, outputType: Type, rawQuery: String): Tree = {
    val xs = imports ++ translateQuery(inputType, outputType, getOrAbort(CqlParser.parseDML(rawQuery)), rawQuery)
    q"{..$xs}"
  }

  def translateQuery(inputType: Type, outputType: Type, query: DataManipulation, raw: String): Seq[Stat] = query match {
    case SelectStatement(mod, selection, from, where, orderBy, perPartitionLimit, limit, allowFiltering) =>
      ImmutableSeq(
        q"type Version = ${Type.Name("1")}",

        q"type Mod = TNone",  //<: Option[Select.Mod],
        q"type Selection = ${translateSelection(selection)}",
        q"type From = TableName[${translateKeyspace(from.keyspace)}, ${literal(from.table)}]",
        q"type Where = ${translateRelations(where.map(_.relations).getOrElse(Seq.empty))}",  //<: WhereClause.Relations,
        q"type OrderBy = TNone",  //<: Option[Select.OrderBy[_]],
        q"type PerPartitionLimit = TNone",  //<: Option[Select.LimitParam],
        q"type Limit = TNone",  //<: Option[Select.LimitParam],
        q"type AllowFiltering = TFalse",  //<: Boolean
        q"type Statement = SelectStatement[Mod, Selection, From, Where, OrderBy, PerPartitionLimit, Limit, AllowFiltering]",

        q"type QueryInputScalaType = ${inputType}",
        q"type QueryOutputRowScalaType = ${outputType}",

        q"Query[Version, Statement, QueryInputScalaType, QueryOutputRowScalaType]($raw)"
      )
  }

  def translateKeyspace(keyspace: Option[KeyspaceName]): Type =
    optional(keyspace.map(k => literal(k.name)))

  def optional(o: Option[Type]): Type =
    o.map(t => t"TSome[$t]").getOrElse(t"TNone")

  def translateSelection(selection: Selection): Type = selection match {
    case Asterisk =>
      notImplemented("translateSelection Asterisk")
    case SelectClause(items) =>
      t[Select.SelectClause[_]](
        foldAsHList(items.map {
          case SelectionClauseItem(selector, None) =>
            translateSelector(selector)
          case SelectionClauseItem(selector, Some(alias)) =>
            notImplemented("translateSelection SelectClause SelectionClauseItem")
        })
      )
  }

  def translateSelectors(selectors: Seq[Selector]): Type =
    foldAsHList(selectors.map(translateSelector))


  def foldAsHList(ts: Seq[Type]): Type = ts.reverse.foldLeft[Type](t"HNil") {
    case (acc: Type, s: Type) =>
      t"$s :: $acc"
  }

  def translateSelector(selector: Selector): Type =
    t[Select.SelectionClauseItem[_, _]](selector match {
    case ColumnName(name)       => t[Select.ColumnName[_]](literal(name))
    case SelectTerm(term)       => notImplemented("translateSelector SelectTerm")
    case Cast(selector, typ)    => notImplemented("translateSelector Cast")
    case troy.cql.ast.dml.Select.Function(fn, params) =>
      t[Select.Function[_, _]](translateFunctionName(fn), translateSelectors(params))
    case Count                  => notImplemented("translateSelector Count")
  }, t[TNone])

  def translateFunctionName(fn: FunctionName): Type =
  t[troy.tast.FunctionName[_, _]](translateKeyspace(fn.keyspace), literal(fn.table))

  def translateRelations(relations: Seq[Relation]): Type =
    foldAsHList(relations.map(translateRelation))

  def translateRelation(relation: Relation): Type = relation match {
    case Relation.Simple(columnName, operator, term) =>
      t[troy.tast.WhereClause.Relation.Simple[_, _, _]](
        literal(columnName),
        translateOperator(operator),
        translateTerm(term)
      )
    case Relation.Tupled(columnNames, operator, terms) => notImplemented("Relation.Tupled")
    case Relation.Token(columnNames, operator, term) => notImplemented("Relation.Token")
  }

  def translateOperator(op: Operator): Type =
    op match {
      case Operator.Equals => t[troy.tast.Operator.Equals]
      case Operator.LessThan => t[troy.tast.Operator.LessThan]
      case Operator.GreaterThan => t[troy.tast.Operator.GreaterThan]
      case Operator.LessThanOrEqual => t[troy.tast.Operator.LessThanOrEqual]
      case Operator.GreaterThanOrEqual => t[troy.tast.Operator.GreaterThanOrEqual]
      case Operator.NotEquals => t[troy.tast.Operator.NotEquals]
      case Operator.In => t[troy.tast.Operator.In]
      case Operator.Contains => t[troy.tast.Operator.Contains]
      case Operator.ContainsKey => t[troy.tast.Operator.ContainsKey]
      case Operator.Like => t[troy.tast.Operator.Like]
    }

  def translateTerm(term: troy.cql.ast.Term): Type = term match {
    case BindMarker.Anonymous => t[troy.tast.BindMarker.Anonymous]
    case BindMarker.Named(name) => t[troy.tast.BindMarker.Named[_]](literal(name))
    case _ => notImplemented("translateTerm not BindMarker")
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
        println(">>>>>>>>>>>>>>>>>>")
        println(tree)
        val q = transform(tree)
        println(">>>>>>>>>>>>>>>>>>")
        println(q)
        println(">>>>>>>>>>>>>>>>>>")
        q
      case _ =>
        abort(???, "")
    }
  }
}