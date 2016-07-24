package troy.macros

import com.datastax.driver.core.{ Session, Cluster }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest._
import scala.concurrent.duration._

trait BaseSpec extends FlatSpec with BeforeAndAfterAll with BeforeAndAfterEach with ScalaFutures with Matchers {
  def port: Int = 9042
  def host: String = "127.0.0.1"

  private val cluster = new Cluster.Builder().addContactPoints(host).withPort(port).build()

  implicit val session: Session = cluster.connect()
  implicit val patienceTimeout = org.scalatest.concurrent.PatienceConfiguration.Timeout(10.seconds)

  override protected def afterAll(): Unit = {
    session.close()
    cluster.close()
    super.afterAll()
  }
}
