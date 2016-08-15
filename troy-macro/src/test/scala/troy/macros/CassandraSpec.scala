package troy.macros

import java.util

import com.datastax.driver.core.{ Session, Cluster }
import org.cassandraunit.CQLDataLoader
import org.cassandraunit.dataset.CQLDataSet
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.concurrent.ScalaFutures
import org.scalatest._
import scala.concurrent.duration._

trait CassandraSpec extends FlatSpec with BeforeAndAfterAll with BeforeAndAfterEach with ScalaFutures with Matchers {
  def port: Int = 9142
  def host: String = "127.0.0.1"

  private lazy val cluster = new Cluster.Builder().addContactPoints(host).withPort(port).build()
  implicit lazy val session: Session = cluster.connect()
  implicit val patienceTimeout = org.scalatest.concurrent.PatienceConfiguration.Timeout(10.seconds)

  def testDataFixtures: String = ""
  private lazy val fixtures = StringCQLDataSet(testDataFixtures, false, false, "test")
  private lazy val schema = new ClassPathCQLDataSet("schema.cql")

  override protected def beforeAll(): Unit = {
    EmbeddedCassandraServerHelper.startEmbeddedCassandra(1.minute.toMillis)
    loadClean()
    super.beforeEach()
  }

  override protected def afterAll(): Unit = {
    session.close()
    cluster.close()
    super.afterAll()
  }

  def loadClean() = {
    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
    loadData(schema, fixtures)
  }

  def loadData(datasets: CQLDataSet*) = {
    val loader = new CQLDataLoader(session)
    datasets.foreach(loader.load)
  }
}

object Helpers {
  def splitStatements(statements: String) =
    statements.split(";").map(_.trim).filter(!_.isEmpty)
}

case class StringCQLDataSet(
    cqlStatements: String,
    isKeyspaceCreation: Boolean,
    isKeyspaceDeletion: Boolean,
    getKeyspaceName: String
) extends CQLDataSet {
  lazy val getCQLStatements = util.Arrays.asList(Helpers.splitStatements(cqlStatements): _*)

}