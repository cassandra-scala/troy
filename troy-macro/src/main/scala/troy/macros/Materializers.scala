package troy.macros

import troy.codecs.TroyCodec
import troy.driver.{ CassandraDataType => CT }

import scala.reflect.macros.blackbox.Context

object Materializers {

  def materializeTroyCodec[S: c.WeakTypeTag, C <: CT: c.WeakTypeTag](c: Context): c.Expr[TroyCodec[S, C]] = {
    import c.universe._

    val scalaPrimitiveToJavaWrappers = Map(
      typeOf[Int] -> typeOf[java.lang.Integer]
    // TODO: Map rest of primitives to their Java wrappers
    )

    def isPrimitive(t: c.universe.Type) =
      scalaPrimitiveToJavaWrappers.keys.find(t.=:=).isDefined

    c.Expr[TroyCodec[S, C]]((c.weakTypeOf[S].dealias, c.weakTypeOf[C].dealias) match {
      case (st, ct) if st <:< typeOf[Seq[_]] && ct <:< typeOf[CT.List[_]] && isPrimitive(st.typeArgs.head) =>
        val ps = st.typeArgs.head
        val pj = scalaPrimitiveToJavaWrappers(ps)
        val pc = ct.typeArgs.head
        q"new troy.codecs.TroyListOfPrimitivesTypeCodec[$pj, $ps, $pc](new troy.codecs.TroyListTypeCodec[$pj, $pc])"
      case (st, ct) if st <:< typeOf[Seq[_]] && ct <:< typeOf[CT.List[_]] =>
        q"new troy.codecs.TroyListTypeCodec[${st.typeArgs.head}, ${ct.typeArgs.head}]"

      case (st, ct) if st <:< typeOf[Set[_]] && ct <:< typeOf[CT.Set[_]] && isPrimitive(st.typeArgs.head) =>
        val ps = st.typeArgs.head
        val pj = scalaPrimitiveToJavaWrappers(ps)
        val pc = ct.typeArgs.head
        q"new troy.codecs.TroySetOfPrimitivesTypeCodec[$pj, $ps, $pc](new troy.codecs.TroySetTypeCodec[$pj, $pc])"
      case (st, ct) if st <:< typeOf[Set[_]] && ct <:< typeOf[CT.Set[_]] =>
        q"new troy.codecs.TroySetTypeCodec[${st.typeArgs.head}, ${ct.typeArgs.head}]"

      case (st, ct) if !isPrimitive(st) =>
        q"troy.codecs.TroyCodec.wrap[$st, $ct]"
    })
  }

}
