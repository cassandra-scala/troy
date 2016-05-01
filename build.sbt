lazy val agamemnon = project
  .copy(id = "agamemnon")
  .in(file("."))
  .configs(IntegrationTest extend(Test))
  .enablePlugins(AutomateHeaderPlugin, GitVersioning)

name := "agamemnon"

libraryDependencies ++= Vector(
  Library.scalaTest % "test",
  Library.scalaReflect,
  Library.scalaParserCombinators,
  Library.cassandraDriverCore
)

unmanagedClasspath in Compile ++= (unmanagedResources in Compile).value

initialCommands := """|import com.abdulradi.agamemnon._
                      |""".stripMargin
