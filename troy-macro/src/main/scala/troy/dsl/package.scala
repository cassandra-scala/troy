package troy

import com.datastax.driver.core.Session
import troy.macros._

package object dsl {

  implicit class RichStringContext(val context: StringContext) extends AnyVal {
    def cql(args: Any*): MacroParam.DslBoundStatement = ???
  }

  def troy[R](code: () => MacroParam[R])(implicit session: Session): () => R = macro troyImpl[() => MacroParam[R]]
  def troy[T, R](code: T => MacroParam[R])(implicit session: Session): T => R = macro troyImpl[T => MacroParam[R]]
  def troy[T1, T2, R](code: (T1, T2) => MacroParam[R])(implicit session: Session): (T1, T2) => R = macro troyImpl[(T1, T2) => MacroParam[R]]

}
