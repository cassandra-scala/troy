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

package troy.driver

import com.datastax.driver.core.{ ResultSet, BoundStatement, Session, TypeCodec }

import scala.concurrent.Future

object PreparedStatement {
  case class ValueCodecPair[T](value: T, codec: TypeCodec[T])
}

case class PreparedStatement(rawStatement: String, session: Session) {
  import PreparedStatement._

  //  def session: Session
  //  def rawStatement: String

  val prepared = session.prepare(rawStatement)

  def bind(values: Seq[ValueCodecPair[_]]): BoundStatement =
    values.zipWithIndex.foldLeft(prepared.bind()) {
      case (bound, (p, i)) => setValue(bound, i, p)
    }

  protected def setValue[T](bound: BoundStatement, i: Int, value: ValueCodecPair[T]): BoundStatement =
    bound.set(i, value.value, value.codec)

  def execute(values: ValueCodecPair[_]*): Future[ResultSet] =
    execute(bind(values))

  protected def execute(bound: BoundStatement): Future[ResultSet] =
    session.executeAsync(bound).asScala
}

//abstract class PreparedStatement1[-T1, C1](rawStatement: String, session: Session)
//  extends PreparedStatement(rawStatement, session) with Function1[T1, Function1[C1, Future[ResultSet]]]
//
//{
//  def apply[T](value: T)(implicit ev: HasCodec[T, C1]) = {
//    execute(Seq(value -> ev.codec))
//  }
//}
