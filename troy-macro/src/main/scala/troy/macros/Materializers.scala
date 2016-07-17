package troy.macros

import troy.driver.{ CassandraDataType => CT }
import troy.dsl.{ TroySetTypeCodec, TroyCodec }

import scala.reflect.macros.blackbox.Context

object Materializers {

  def materializeTroyCodec[S: c.WeakTypeTag, C <: CT: c.WeakTypeTag](c: Context): c.Expr[TroyCodec[S, C]] = {
    import c.universe._

    c.Expr[TroyCodec[S, C]]((c.weakTypeOf[S].dealias, c.weakTypeOf[C].dealias) match {
      case (st, ct) if st <:< typeOf[Set[_]] && ct <:< typeOf[CT.Set[_]] =>
        q"new TroySetTypeCodec[${st.typeArgs.head}, ${ct.typeArgs.head}]"
      case x =>
        println(x)
        ???
    })
  }

}
