package troy
package meta

import troy.cql.ast._
import troy.cql.ast.dml.Operator
import troy.cql.ast.dml.Select._

import scala.collection.immutable.{Seq => ImmutableSeq}
import troy.cql.ast.dml.WhereClause.Relation

import scala.meta._

object QueryUtils extends Utils {

  val imports = ImmutableSeq(
    q"import troy.driver.query.select._",
    q"import Select._",
    q"import shapeless._"
  )
  def constructQuery(inputType: Type, outputType: Type, rawQuery: String): Tree = {
    val xs = imports ++ translateQuery(inputType, outputType, getOrAbort(CqlParser.parseDML(rawQuery)), rawQuery)
    q"{..$xs}"
  }

  def translateQuery(inputType: Type, outputType: Type, query: DataManipulation, raw: String): Seq[Stat] = query match {
    case SelectStatement(mod, selection, from, where, orderBy, perPartitionLimit, limit, allowFiltering) =>
      ImmutableSeq(
        q"type Version = ${Type.Name("1")}",
        q"type Keyspace = ${translateKeyspace(from.keyspace)}",
        q"type Table = ${literal(from.table)}",
        q"type SelectonClause = ${translateSelection(selection)}",
        q"type Relations = ${translateRelations(where.map(_.relations).getOrElse(Seq.empty))}",
        q"type QueryInputScalaType = ${inputType}",
        q"type QueryOutputRowScalaType = ${outputType}",
        q"Query[Version, Keyspace, Table, SelectonClause, Relations, QueryInputScalaType, QueryOutputRowScalaType]($raw)"
      )
  }

  def translateKeyspace(keyspace: Option[KeyspaceName]): Type =
    keyspace.map(k => t"${literal(k.name)}").getOrElse(t"NoKeyspace")

  def translateSelection(selection: Selection): Type = selection match {
    case Asterisk =>
      notImplemented("translateSelection Asterisk")
    case SelectClause(items) =>
      foldAsHList(items.map {
        case SelectionClauseItem(selector, None) =>
          translateSelector(selector)
        case SelectionClauseItem(selector, Some(alias)) =>
          notImplemented("translateSelection SelectClause SelectionClauseItem")
      })
  }

  def translateSelectors(selectors: Seq[Selector]): Type =
    foldAsHList(selectors.map(translateSelector))


  def foldAsHList(ts: Seq[Type]): Type = ts.reverse.foldLeft[Type](t"HNil") {
    case (acc: Type, s: Type) =>
      t"$s :: $acc"
  }

  def translateSelector(selector: Selector): Type = selector match {
    case ColumnName(name)       => t"Column[${literal(name)}]"
    case SelectTerm(term)       => notImplemented("translateSelector SelectTerm")
    case Cast(selector, typ)    => notImplemented("translateSelector Cast")
    case troy.cql.ast.dml.Select.Function(FunctionName(keyspace, name), params) =>
      t"Function[${translateKeyspace(keyspace)}, ${literal(name)}, ${translateSelectors(params)}]"
    case Count                  => notImplemented("translateSelector Count")
  }

  def translateRelations(relations: Seq[Relation]): Type =
    foldAsHList(relations.map(translateRelation))

  def translateRelation(relation: Relation): Type = relation match {
    case Relation.Simple(columnName, operator, term) =>
      t"Relation[Column[${literal(columnName)}] :: HNil, ${translateOperator(operator)}, ${translateTerm(term)} :: HNil]"
    case Relation.Tupled(columnNames, operator, terms) => notImplemented("Relation.Tupled")
    case Relation.Token(columnNames, operator, term) => notImplemented("Relation.Token")
  }

  def translateOperator(op: Operator): Type =
    op match {
      case Operator.Equals => t"Equality"
      case Operator.LessThan => t"Equality"
      case Operator.GreaterThan => t"Equality"
      case Operator.LessThanOrEqual => t"Equality"
      case Operator.GreaterThanOrEqual => t"Equality"
      case Operator.NotEquals => t"Equality"
      case Operator.In => t"In"
      case Operator.Contains => t"Contains"
      case Operator.ContainsKey => t"ContainsKey"
      case Operator.Like => t"Like"
    }

  def translateTerm(t: troy.cql.ast.Term): Type = t match {
    case BindMarker.Anonymous => t"AnonymousBindMarker"
    case BindMarker.Named(name) => t"NamedBindMarker[${literal(name)}]"
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