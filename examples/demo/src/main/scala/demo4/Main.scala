package demo4

import java.util.UUID
import com.datastax.driver.core.{Cluster, Session}
import troy.dsl._
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

  val listByTag = withSchema {
    (tag: String) =>
      cql"""
         SELECT post_id, post_title
         FROM test.posts
         WHERE post_tags CONTAINS $tag
       """.prepared.as(Post)
  }

  val titlesF = listByTag("test").map(_.map(_.title))
  val titles = Await.result(titlesF, Duration(1, "second"))
  println("Matching titles:")
  titles.foreach(println)

  session.close()
  cluster.close()
}
