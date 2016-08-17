package demo1

import com.datastax.driver.core.{Cluster, Session}
import troy.dsl._

object Main extends App {
  val port: Int = 9042
  val host: String = "127.0.0.1"

  private val cluster =
    new Cluster.Builder().addContactPoints(host).withPort(port).build()

  implicit val session: Session = cluster.connect()

  val listByAuthor = withSchema {
    (authorId: String) =>
      cql"""
         SELECT post_id, post_title
         FROM test.posts
         WHERE author_id = $authorId
       """.prepared
  }


  println(session.execute(listByAuthor("test")))

  session.close()
  cluster.close()
}
