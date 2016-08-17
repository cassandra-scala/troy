package demo5

import java.util.UUID
import com.datastax.driver.core.{Cluster, Session}
import troy.dsl._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

case class Post(id: UUID, comments: Map[Integer, String]) // Map doesn't work with Primitives yet. See: https://github.com/cassandra-scala/troy/issues/18

object Main extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  val port: Int = 9042
  val host: String = "127.0.0.1"

  private val cluster =
    new Cluster.Builder().addContactPoints(host).withPort(port).build()

  implicit val session: Session = cluster.connect()

  val getCommentsByLine = withSchema {
    (authorId: String, postId: UUID, line: Int) =>
      cql"""
         SELECT post_id, comments
         FROM test.posts
         WHERE author_id = $authorId
           AND post_id = $postId
           AND comments CONTAINS KEY $line
       """.prepared.as(Post)
  }

  val postId = UUID.fromString("a4a70900-24e1-11df-8924-001ff3591711")
  println(Await.result(getCommentsByLine("test", postId, 5), Duration(1, "second")))

  session.close()
  cluster.close()
}
