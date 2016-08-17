scalaVersion := "2.11.8"

//resolvers += Resolver.bintrayRepo("tabdulradi", "maven")

libraryDependencies += "com.abdulradi" %% "troymacro" % "0.0.2-SNAPSHOT"

unmanagedClasspath in Compile ++= (unmanagedResources in Compile).value
