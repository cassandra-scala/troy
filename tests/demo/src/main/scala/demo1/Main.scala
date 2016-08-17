package demo1

import java.util.UUID

import com.datastax.driver.core.{Cluster, Session}
import troy.dsl._

case class Post(id: UUID, title: String)

object Main extends App {
  val port: Int = 9042
  val host: String = "127.0.0.1"

  private val cluster =
    new Cluster.Builder().addContactPoints(host).withPort(port).build()

  implicit val session: Session = cluster.connect()

  /*
  SELECT post_id, post_title 
       FROM test.posts 
       WHERE author_id
   */
//  val listByAuthor = withSchema {
//    (authorId: String) =>
//      cql"""
//         SELECT post_id, post_title
//         FROM test.posts
//         WHERE author_id = $authorId
//       """.prepared
//  }

  val listByAuthor = {
    import _root_.troy.dsl.InternalDsl._
    import _root_.troy.driver.CassandraDataType

    val prepared = implicitly[com.datastax.driver.core.Session].prepare("""
        SELECT post_id, post_title
        FROM test.posts
        WHERE author_id = ?
    """)

    (authorId: String) =>
      bind(prepared, param(authorId).as[CassandraDataType.Text])
  }


  println(session.execute(listByAuthor("test")))

  session.close()
  cluster.close()
}
