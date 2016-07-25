package troy.macros

import com.datastax.driver.core.{ Session, Cluster }
import org.cassandraunit.CQLDataLoader
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.concurrent.ScalaFutures
import org.scalatest._
import scala.concurrent.duration._

trait BaseSpec extends FlatSpec with BeforeAndAfterAll with BeforeAndAfterEach with ScalaFutures with Matchers {
  def port: Int = 9142
  def host: String = "127.0.0.1"

  EmbeddedCassandraServerHelper.startEmbeddedCassandra(1.minute.toMillis)
  private val cluster = new Cluster.Builder().addContactPoints(host).withPort(port).build()
  implicit val session: Session = cluster.connect()
  implicit val patienceTimeout = org.scalatest.concurrent.PatienceConfiguration.Timeout(10.seconds)

  def cassandraDataFixtures: String = ""

  override protected def beforeEach(): Unit = {
    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
    loadSchema()
    loadData()
    super.beforeEach()
  }

  override protected def afterAll(): Unit = {
    session.close()
    cluster.close()
    super.afterAll()
  }

  def loadSchema() =
    new CQLDataLoader(session).load(new ClassPathCQLDataSet("schema.cql"))

  def loadData() =
    execAll(cassandraDataFixtures)

  def execAll(statements: String): Unit =
    execAll(splitStatements(statements))

  def execAll(statements: Seq[String]): Unit =
    statements.foreach(session.execute)

  def splitStatements(statements: String) =
    statements.split(";").map(_.trim).filter(!_.isEmpty)
}
