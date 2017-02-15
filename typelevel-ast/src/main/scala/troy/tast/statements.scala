package troy.tast

import troy.tutils.{ TBoolean, TOption }

sealed trait DataManipulationStatement

sealed trait SelectStatement[Mod <: TOption[Select.Mod], Selection <: Select.Selection, From <: TableName[_, _], Where <: WhereClause.Relations, OrderBy <: TOption[Select.OrderBy[_]], PerPartitionLimit <: TOption[Select.LimitParam], Limit <: TOption[Select.LimitParam], AllowFiltering <: TBoolean] extends DataManipulationStatement

sealed trait InsertStatement[Into <: TableName[_, _], InsertClause <: Insert.InsertClause, IfNotExists <: Boolean, Using <: THList[UpdateParam]] extends DataManipulationStatement

sealed trait UpdateStatement[T <: TableName[_, _], Using <: THList[UpdateParam], Set <: THList[Update.Assignment], Where <: WhereClause.Relations, IfCondition <: TOption[IfExistsOrCondition]] extends DataManipulationStatement

sealed trait DeleteStatement[SimpleSelection <: THList[SimpleSelection], From <: TableName[_, _], Using <: THList[UpdateParam], Where <: WhereClause.Relations, IfCondition <: TOption[IfExistsOrCondition]] extends DataManipulationStatement