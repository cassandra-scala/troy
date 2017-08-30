package demo1

import java.util.UUID
import com.datastax.driver.core.{Cluster, Session}
import troy.dsl._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

case class Post(id: UUID, img: String)

object Main extends App {

  val port: Int = 9042
  val host: String = "127.0.0.1"

  private val cluster =
    new Cluster.Builder().addContactPoints(host).withPort(port).build()

  implicit val session: Session = cluster.connect()

  val getCommentsByLine = withSchema.minVersion(1) {
    (authorId: String, postId: UUID) =>
      cql"""
         SELECT post_id, post_img
         FROM test.posts
         WHERE author_id = $authorId
           AND post_id = $postId
       """.prepared.as(Post)
  }

  val postId = UUID.fromString("a4a70900-24e1-11df-8924-001ff3591711")
  println(Await.result(getCommentsByLine("test", postId), Duration(1, "second")))

  session.close()
  cluster.close()
}
