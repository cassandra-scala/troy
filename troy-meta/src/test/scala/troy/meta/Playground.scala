package troy
package meta

import java.util.UUID

import com.datastax.driver.core.utils.UUIDs

import scala.concurrent.Future

@schema object Schema

object Playground extends App {
  def query[I, O](x: String): I => Future[O] = ???
  case class Post(id: UUID, title: String)

  import Schema._
  @schemasafe val getAuthorPosts = query[(UUID, Int), Post]("select post_id, post_title from test.posts where authorId = ? AND post_rating >= ?;")
  getAuthorPosts((UUIDs.timeBased(), 4)): Future[Post]
}
