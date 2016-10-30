package troy.driver.codecs

import com.datastax.driver.core._
import scala.annotation.implicitNotFound
import scala.collection.JavaConverters._
import troy.driver.{ CassandraDataType => CT }

@implicitNotFound("Incompatible column type ${S} <--> ${C}")
trait TroyCodec[S, C <: CT] {
  def get(gettable: GettableByIndexData, i: Int): S
  def set[T <: SettableByIndexData[T]](settable: T, i: Int, value: S): T
}

object TroyCodec {

  def apply[S <: AnyRef, C <: CT](typeCodec: TypeCodec[S]) =
    new TroyCodec[S, C] {
      override def set[T <: SettableByIndexData[T]](settable: T, i: Int, value: S) = settable.set(i, value, typeCodec)
      override def get(gettable: GettableByIndexData, i: Int) = gettable.get(i, typeCodec)
    }

  implicit def wrapJavaTypeCodecs[S <: AnyRef, C <: CT](implicit hasTypeCodec: HasTypeCodec[S, C]) =
    TroyCodec[S, C](hasTypeCodec.typeCodec)

  implicit def wrapOptional[S <: AnyRef, C <: CT](implicit hasTypeCodec: HasTypeCodec[S, C]) =
    TroyCodec[Option[S], C](new OptionTypeCodec(hasTypeCodec.typeCodec))

  implicit def listOfNonPrimitives[S <: AnyRef, C <: CT.Native](implicit inner: HasTypeCodec[S, C]) =
    new TroyCodec[Seq[S], CT.List[C]] {
      val codec = TypeCodec.list(inner.typeCodec)
      override def set[T <: SettableByIndexData[T]](settable: T, i: Int, value: Seq[S]) = settable.set(i, value.asJava, codec)
      override def get(gettable: GettableByIndexData, i: Int) = gettable.get(i, codec).asScala
    }

  implicit def listOfPrimitives[J <: AnyRef, S <: AnyVal, C <: CT.Native](implicit inner: TroyCodec[Seq[J], CT.List[C]], converter: PrimitivesConverter[J, S]) =
    new TroyCodec[Seq[S], CT.List[C]] {
      override def set[T <: SettableByIndexData[T]](settable: T, i: Int, value: Seq[S]) = inner.set(settable, i, value.map(converter.toJava))
      override def get(gettable: GettableByIndexData, i: Int) = inner.get(gettable, i).map(converter.toScala)
    }

  implicit def setOfNonPrimitives[S <: AnyRef, C <: CT.Native](implicit inner: HasTypeCodec[S, C]) =
    new TroyCodec[Set[S], CT.Set[C]] {
      val codec = TypeCodec.set(inner.typeCodec)
      override def set[T <: SettableByIndexData[T]](settable: T, i: Int, value: Set[S]) = settable.set(i, value.asJava, codec)
      override def get(gettable: GettableByIndexData, i: Int) = gettable.get(i, codec).asScala.toSet
    }

  implicit def setOfPrimitives[J <: AnyRef, S <: AnyVal, C <: CT.Native](implicit inner: TroyCodec[Set[J], CT.Set[C]], converter: PrimitivesConverter[J, S]) =
    new TroyCodec[Set[S], CT.Set[C]] {
      override def set[T <: SettableByIndexData[T]](settable: T, i: Int, value: Set[S]) = inner.set(settable, i, value.map(converter.toJava))
      override def get(gettable: GettableByIndexData, i: Int) = inner.get(gettable, i).map(converter.toScala)
    }

  implicit def mapOfNonPrimitives[KS <: AnyRef, KC <: CT.Native, VS <: AnyRef, VC <: CT.Native](implicit keyInner: HasTypeCodec[KS, KC], valueInner: HasTypeCodec[VS, VC]) =
    new TroyCodec[Map[KS, VS], CT.Map[KC, VC]] {
      val codec = TypeCodec.map(keyInner.typeCodec, valueInner.typeCodec)
      override def set[T <: SettableByIndexData[T]](settable: T, i: Int, value: Map[KS, VS]) = settable.set(i, value.asJava, codec)
      override def get(gettable: GettableByIndexData, i: Int) = gettable.get(i, codec).asScala.toMap
    }

  implicit def mapOfPrimitives[KJ, KS, KC <: CT.Native, VJ, VS, VC <: CT.Native](implicit inner: TroyCodec[Map[KJ, VJ], CT.Map[KC, VC]], converter: PrimitivesConverter[(KJ, VJ), (KS, VS)]) =
    new TroyCodec[Map[KS, VS], CT.Map[KC, VC]] {
      override def set[T <: SettableByIndexData[T]](settable: T, i: Int, value: Map[KS, VS]) = inner.set(settable, i, value.map(converter.toJava))
      override def get(gettable: GettableByIndexData, i: Int) = inner.get(gettable, i).map(converter.toScala)
    }
}
