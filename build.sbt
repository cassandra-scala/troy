lazy val agamemnon = project
  .copy(id = "agamemnon")
  .in(file("."))
  .enablePlugins(AutomateHeaderPlugin, GitVersioning)

name := "agamemnon"

libraryDependencies ++= Vector(
  Library.scalaTest % "test",
  Library.scalaReflect,
  Library.scalaParserCombinators
)

unmanagedClasspath in Compile ++= (unmanagedResources in Compile).value

initialCommands := """|import com.abdulradi.agamemnon._
                      |""".stripMargin
