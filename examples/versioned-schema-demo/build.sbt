scalaVersion := "2.12.0"

resolvers += Resolver.bintrayRepo("tabdulradi", "maven")

libraryDependencies += "io.github.cassandra-scala" %% "troy" % "0.5.0"

unmanagedClasspath in Compile ++= (unmanagedResources in Compile).value
