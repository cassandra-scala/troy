package troy
package meta

import java.util.UUID

import com.datastax.driver.core.{ Cluster, Session }
import com.datastax.driver.core.utils.UUIDs

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }
import scala.util.Try

@schema object Schema

object Playground extends App {
  def query[I, O](x: String): I => Future[Seq[O]] = ???
  case class Post(id: UUID, title: String)

  import Schema._

  withSession { implicit session =>
    import scala.concurrent.ExecutionContext.Implicits.global

    @schemasafe val getAuthorPosts =
      query[(UUID, Int), Post]("select post_id, post_title from test.posts where author_id = ? AND post_rating >= ? ALLOW FILTERING;")

    val authorId = UUID.fromString("6287c470-e298-11e6-9b3d-ffeaf4ddcb54")
    println(Await.result(getAuthorPosts((authorId, 4)): Future[Iterable[Post]], Duration.Inf))
  }

  def withSession[T](f: Session => T) = {
    val cluster = new Cluster.Builder().addContactPoints("127.0.0.1").withPort(9042).build()
    val session: Session = cluster.connect()
    Try(f(session))
    session.close()
    cluster.close()
  }
}
