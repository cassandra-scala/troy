package troy.driver.codecs

import com.datastax.driver.core._
import scala.annotation.implicitNotFound
import scala.collection.JavaConverters._
import troy.driver.{ CassandraDataType => CT }

@implicitNotFound("Incompatible column type ${S} <--> ${C}")
trait TroyCodec[C <: CT, S] {
  def get(gettable: GettableByIndexData, i: Int): S
  def set[T <: SettableByIndexData[T]](settable: T, i: Int, value: S): T
}

object TroyCodec {

  def instance[C <: CT, S](typeCodec: TypeCodec[S]) =
    new TroyCodec[C, S] {
      override def set[T <: SettableByIndexData[T]](settable: T, i: Int, value: S) = settable.set(i, value, typeCodec)
      override def get(gettable: GettableByIndexData, i: Int) = gettable.get(i, typeCodec)
    }

  implicit def wrapJavaTypeCodecs[S <: AnyRef, C <: CT](implicit hasTypeCodec: HasTypeCodec[S, C]) =
    instance[C, S](hasTypeCodec.typeCodec)

  implicit def wrapOptional[C <: CT, S <: AnyRef](implicit hasTypeCodec: HasTypeCodec[S, C]) =
    instance[C, Option[S]](new OptionTypeCodec(hasTypeCodec.typeCodec))

  implicit def listOfNonPrimitives[C <: CT.Native, S <: AnyRef](implicit inner: HasTypeCodec[S, C]) =
    new TroyCodec[CT.List[C], Seq[S]] {
      val codec = TypeCodec.list(inner.typeCodec)
      override def set[T <: SettableByIndexData[T]](settable: T, i: Int, value: Seq[S]) = settable.set(i, value.asJava, codec)
      override def get(gettable: GettableByIndexData, i: Int) = gettable.get(i, codec).asScala
    }

  implicit def listOfPrimitives[J <: AnyRef, S <: AnyVal, C <: CT.Native](implicit inner: TroyCodec[CT.List[C], Seq[J]], converter: PrimitivesConverter[J, S]) =
    new TroyCodec[CT.List[C], Seq[S]] {
      override def set[T <: SettableByIndexData[T]](settable: T, i: Int, value: Seq[S]) = inner.set(settable, i, value.map(converter.toJava))
      override def get(gettable: GettableByIndexData, i: Int) = inner.get(gettable, i).map(converter.toScala)
    }

  implicit def setOfNonPrimitives[S <: AnyRef, C <: CT.Native](implicit inner: HasTypeCodec[S, C]) =
    new TroyCodec[CT.Set[C], Set[S]] {
      val codec = TypeCodec.set(inner.typeCodec)
      override def set[T <: SettableByIndexData[T]](settable: T, i: Int, value: Set[S]) = settable.set(i, value.asJava, codec)
      override def get(gettable: GettableByIndexData, i: Int) = gettable.get(i, codec).asScala.toSet
    }

  implicit def setOfPrimitives[J <: AnyRef, S <: AnyVal, C <: CT.Native](implicit inner: TroyCodec[CT.Set[C], Set[J]], converter: PrimitivesConverter[J, S]) =
    new TroyCodec[CT.Set[C], Set[S]] {
      override def set[T <: SettableByIndexData[T]](settable: T, i: Int, value: Set[S]) = inner.set(settable, i, value.map(converter.toJava))
      override def get(gettable: GettableByIndexData, i: Int) = inner.get(gettable, i).map(converter.toScala)
    }

  implicit def mapOfNonPrimitives[KS <: AnyRef, KC <: CT.Native, VS <: AnyRef, VC <: CT.Native](implicit keyInner: HasTypeCodec[KS, KC], valueInner: HasTypeCodec[VS, VC]) =
    new TroyCodec[CT.Map[KC, VC], Map[KS, VS]] {
      val codec = TypeCodec.map(keyInner.typeCodec, valueInner.typeCodec)
      override def set[T <: SettableByIndexData[T]](settable: T, i: Int, value: Map[KS, VS]) = settable.set(i, value.asJava, codec)
      override def get(gettable: GettableByIndexData, i: Int) = gettable.get(i, codec).asScala.toMap
    }

  implicit def mapOfPrimitives[KJ, KS, KC <: CT.Native, VJ, VS, VC <: CT.Native](implicit inner: TroyCodec[CT.Map[KC, VC], Map[KJ, VJ]], converter: PrimitivesConverter[(KJ, VJ), (KS, VS)]) =
    new TroyCodec[CT.Map[KC, VC], Map[KS, VS]] {
      override def set[T <: SettableByIndexData[T]](settable: T, i: Int, value: Map[KS, VS]) = inner.set(settable, i, value.map(converter.toJava))
      override def get(gettable: GettableByIndexData, i: Int) = inner.get(gettable, i).map(converter.toScala)
    }
}
