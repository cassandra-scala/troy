package demo99

import java.util.UUID

import com.datastax.driver.core._
import troy.dsl._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}


case class Post(
                 id: UUID,
                 authorName: String,
                 reviewerName: Option[String],
                 title: String,
                 rating: Option[Int],
                 tags: Seq[String]
               )

class PostService(implicit session: Session, ec: ExecutionContext) {
  // val create = withSchema { (authorId: String, post: Post) =>
  //   cql""
  //     .prepared
  //     .as(Post)
  // }

  val get = withSchema { (authorId: String, postId: UUID) =>
    cql"""
      SELECT post_id, author_name, reviewer_name, post_title, post_rating, post_tags
      FROM test.posts
      WHERE author_id = $authorId AND post_id = $postId;
    """.prepared.as(Post)
  }
}

object Main extends App {
  val port: Int = 9042
  val host: String = "127.0.0.1"

  private val cluster =
    new Cluster.Builder().addContactPoints(host).withPort(port).build()

  val session: Session = cluster.connect()

  val posts = new PostService()(session, ExecutionContext.global)
  val result = posts.get("1", UUID.randomUUID())
  println(Await.result(result, Duration(1, "second")))

  session.close()
  cluster.close()
}
