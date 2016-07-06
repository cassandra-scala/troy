package troy

import com.datastax.driver.core.Session
import troy.macros._

package object dsl {

  implicit class RichStringContext(val context: StringContext) extends AnyVal {
    def cql(args: Any*): MacroParam.DslBoundStatement = ???
  }

  def troy[T, R](code: T => MacroParam[R])(session: Session): T => R = macro troyImpl[T => MacroParam[R]]

}
