package demo99

import java.util.UUID

import com.datastax.driver.core._
import troy.dsl._
import troy.driver.DSL._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.ExecutionContext.Implicits.global


case class Post(
                 id: UUID,
                 authorName: String,
                 reviewerName: Option[String],
                 title: String,
                 rating: Option[Int],
                 tags: Seq[String]
               )

class PostService(implicit session: Session, ec: ExecutionContext) {
  val create = withSchema { (authorId: String, title: String) =>
    cql"""
       INSERT INTO test.posts (author_id , post_id , post_title )
       VALUES ( $authorId, now(), $title);
     """.prepared.executeAsync
  }

  val get = withSchema { (authorId: String, postId: UUID) =>
    cql"""
      SELECT post_id, author_name, reviewer_name, post_title, post_rating, post_tags
      FROM test.posts
      WHERE author_id = $authorId AND post_id = $postId;
    """.prepared.as(Post)
  }

  val listByAuthor = withSchema { (authorId: String) =>
      cql"""
         SELECT post_id, author_name, reviewer_name, post_title, post_rating, post_tags
         FROM test.posts
         WHERE author_id = $authorId
       """
        .prepared
        .executeAsync
        .as(Post)
  }

  val update = withSchema { (authorId: String, postId: UUID, newTitle: String) =>
    cql"""
       UPDATE test.posts SET post_title = $newTitle WHERE author_id = $authorId AND post_id = $postId;
     """.prepared.executeAsync
  }

  val delete = withSchema { (authorId: String, postId: UUID) =>
    cql"""
       DELETE FROM test.posts WHERE author_id = $authorId AND post_id = $postId;
     """.prepared.executeAsync
  }

}

object Main extends App {
  val port: Int = 9042
  val host: String = "127.0.0.1"

  private val cluster =
    new Cluster.Builder().addContactPoints(host).withPort(port).build()

  implicit val session: Session = cluster.connect()

  val postService = new PostService()


  val result = for {
    _ <- postService.create("test", "title")
    posts <- postService.listByAuthor("test")
    postId = posts.head.id
    _ <- postService.update("test", postId, "new title")
    updatedPost <- postService.get("test", postId)
    _ <- postService.delete("test", postId)
    deletedPosts <- postService.listByAuthor("test")
  } yield (posts, updatedPost, deletedPosts)

  val (posts, updatedPost, deletedPosts) = Await.result(result, Duration(1, "second"))

  println(posts)
  println(updatedPost)
  println(deletedPosts)

  session.close()
  cluster.close()
}
