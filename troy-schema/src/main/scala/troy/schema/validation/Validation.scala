package troy
package schema.validation

import troy.cql.ast.{ DataDefinition, DataManipulation }
import troy.schema._

trait Validation[-T] {
  def validate(statement: T): Result[Iterable[Message]] =
    rules.applyOrElse(statement, (_: T) => noMessages)

  protected[this] def rules: PartialFunction[T, Result[Iterable[Message]]]

  protected[this] val noMessages: Result[Seq[Message]] = V.success(Seq.empty)
}

class Validations(schema: Schema, levelConfig: Message => Validations.Level) {
  def validate(statement: DataManipulation): Result[_] =
    validate(dmValidations, statement)

  def validate(statement: DataDefinition): Result[_] =
    validate(ddValidations, statement)

  protected[this] def validate[T](validations: Seq[Validation[T]], statement: T): Result[_] =
    V.merge(validations.map(_.validate(statement).flatMap(adjustMessageLevels)))

  protected[this] def adjustMessageLevels(msgs: Iterable[Message]) = {
    val groupedMsgs = groupByLevel(msgs)
    val warns = groupedMsgs.getOrElse(Validations.Warn, Iterable.empty)
    val errors = groupedMsgs.getOrElse(Validations.Error, Iterable.empty)

    V.merge(emptyResponse.addWarns(warns) +: errors.map(e => V.error(e)).toSeq)
  }

  protected[this] def groupByLevel(msgs: Iterable[Message]) =
    msgs.map(pairWithLevel).groupBy(_._1).mapValues(_.map(_._2))

  protected[this] def pairWithLevel(m: Message) = levelConfig(m) -> m

  protected[this] val dmValidations = Seq(
    new SelectDistinctNonStaticColumns(schema),
    new WhereNonPrimaryNoIndex(schema)
  )

  protected[this] val ddValidations = Seq(
    new AlterColumnIncompatibleTypes(schema)
  )

  protected[this] val emptyResponse: Result[Unit] = V.success(())
}

object Validations {
  def apply(schema: Schema) = new Validations(schema, m => levelConfig(m.productPrefix))

  sealed trait Level
  case object Warn extends Level
  case object Error extends Level
  case object Off extends Level

  val levelConfig: Map[String, Level] = Map( // TODO: Read from config file
    "SelectedDistinctNonStaticColumn" -> Error
  ).withDefaultValue(Error)
}
