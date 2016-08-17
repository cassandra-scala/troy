package demo2

import java.util.UUID
import com.datastax.driver.core.{Row, Cluster, Session}
import troy.dsl._
import troy.driver.DSL._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

case class Post(id: UUID, title: String)

object Main extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

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
       """
        .prepared
        .executeAsync
        .as(Post)
  }

  val result = listByAuthor("test")
  println(Await.result(result, Duration(1, "second")))

  session.close()
  cluster.close()
}
