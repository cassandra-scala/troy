package demo3

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

  val get = withSchema {
    (authorId: String, postId: UUID) =>
      cql"""
         SELECT post_id, post_title
         FROM test.posts
         WHERE author_id = $authorId AND post_id = $postId
       """
        .prepared
        .executeAsync
        .oneOption
        .as(Post)
  }

  val result = get("test", UUID.fromString("a4a70900-24e1-11df-8924-001ff3591711"))
  val maybePost: Option[Post] = Await.result(result, Duration(1, "second"))
  println(maybePost.map(_.title).getOrElse("Post not found"))

  session.close()
  cluster.close()
}
