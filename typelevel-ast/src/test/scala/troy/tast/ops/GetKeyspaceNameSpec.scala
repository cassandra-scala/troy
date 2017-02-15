package troy.tast.ops

import troy.tast._
import troy.tutils._

object GetKeyspaceNameSpec {
  import troy.tutils.TypeLevelMatchers._

  def test[S <: SelectStatement[_, _, _, _, _, _, _, _]](implicit x: GetKeyspaceName[S]): x.Out = null.asInstanceOf[x.Out]

  {
    type SelectStatement1 = SelectStatement[TNone, Select.Asterisk, TableName[TSome["my_keyspace"], "my_table"], HNil, TNone, TNone, TNone, TFalse]
    GetKeyspaceName[SelectStatement1].outShouldBe[TSome["my_keyspace"]]
    test[SelectStatement1] shouldBe t[TSome["my_keyspace"]]
  }

  {
    type Version = 1
    type Mod = TNone
    type Selection = Select.SelectClause[Select.SelectionClauseItem[Select.ColumnName["post_id"], TNone] :: Select.SelectionClauseItem[Select.ColumnName["post_titles"], troy.tutils.TNone] :: HNil]
    type From = TableName[TSome["test"], "posts"]
    type Where = WhereClause.Relation.Simple["author_id", troy.tast.Operator.Equals, troy.tast.BindMarker.Anonymous] :: WhereClause.Relation.Simple["post_rating", troy.tast.Operator.GreaterThanOrEqual, troy.tast.BindMarker.Anonymous] :: HNil
    type OrderBy = TNone
    type PerPartitionLimit = TNone
    type Limit = TNone
    type AllowFiltering = TFalse
    type Statement = SelectStatement[Mod, Selection, From, Where, OrderBy, PerPartitionLimit, Limit, AllowFiltering]

    GetKeyspaceName[Statement].outShouldBe[TSome["test"]]
    test[Statement] shouldBe t[TSome["test"]]
  }
}
