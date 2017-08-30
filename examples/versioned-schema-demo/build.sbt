scalaVersion := "2.12.0"

resolvers += Resolver.bintrayRepo("tabdulradi", "maven")

libraryDependencies += "io.github.cassandra-scala" %% "troy" % "0.6.0-SNAPSHOT"

unmanagedClasspath in Compile ++= (unmanagedResources in Compile).value
