/*
 * Copyright 2016 Tamer AbdulRadi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package troy.api

import _root_.schemasafe.core.{Fragment, Query, Materializer}
import _root_.schemasafe.core.utils.XResult
import singleton.ops.XString
import troy.schema._

/** 
  * Single kitchen sink import: `import troy.api.schemasafe._`
  * Provides all necessary functions and implicits to use Troy
  */
package object schemasafe {

  def q[Q <: XString](q: Q): Fragment[Q] = Fragment(q)

  @SuppressWarnings(Array("org.wartremover.warts.Nothing", "org.wartremover.warts.ExplicitImplicitTypes", "org.wartremover.warts.PublicInference"))
  implicit def materializerInstance[LQ <: XString, I, O, QS <: XResult[QuerySignature[_, _, _]], Q <: XResult[Query[LQ, I, O, _, _, _, _]]](
    implicit
    cqm: CassandraQueryAnalyser.Aux[LQ, QS],
    xx: xresultops.FlatMap.Aux[QS, ({type L[A <: QuerySignature[_, _, _], B <: XResult[QuerySignature[_, _, _]]] = CodecFinder.Aux[A, I, O, B]})#L, Q]
  ): Materializer.Aux[LQ, I, O, Q] =
    ???
}

trait CodecFinder[QS <: QuerySignature[_, _, _], I, O] {
  type Out <: XResult[Query[_, _, _, _, _, _, _]]
}

object CodecFinder {
  type Aux[QS <: QuerySignature[_, _, _], I, O, _Out <: XResult[Query[_, _, _, _, _, _, _]]] =
    CodecFinder[QS, I, O]{ type Out = _Out }

  def instance[QS <: QuerySignature[_, _, _], I, O, _Out <: XResult[Query[_, _, _, _, _, _, _]]] =
    new CodecFinder[QS, I, O]{ type Out = _Out }
}

object xresultops {
  trait FlatMap[XR <: XResult[_], TCAux[_, _]] {
    type Out <: XResult[_]
  }
  object FlatMap {
    type Aux[XR <: XResult[_], TCAux[_, _], O <: XResult[_]] =
      FlatMap[XR, TCAux] {
        type Out = O
      }

    def instance[XR <: XResult[_], TCAux[_, _], O <: XResult[_]] =
      new FlatMap[XR, TCAux] {
        override type Out = O
      }
  }
}