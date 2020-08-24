name                := "InfoFlow"
version             := "1.1.1"
scalaVersion        := "2.11.7" //"2.12.1"
parallelExecution   := false
libraryDependencies ++= Seq(
        "org.apache.spark" %% "spark-core" % "2.1.1" % "provided",
        "org.scalatest" %% "scalatest" % "3.0.1" % "test",
        "org.apache.spark" %% "spark-sql" % "2.1.1" % "provided"
)



assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false, includeDependency = true)

mainClass in assembly := Some("InfoFlowMain")

assemblyMergeStrategy in assembly := {

        case PathList("META-INF", "services", "org.apache.hadoop.fs.FileSystem") => MergeStrategy.concat
        case PathList("META-INF", "services", "org.apache.spark.sql.sources.DataSourceRegister") => MergeStrategy.concat
        case PathList("META-INF", xs@_*) => MergeStrategy.discard
        case _ => MergeStrategy.first

}

//logLevel := Level.Error

lazy val build = taskKey[Unit]("Build the project and move the Project Jar to Pack root")

build := {
        // val log = streams.value.log // Use this if logging is needed, this only works within Tasks like this outside it will not work
        assembly.value
        assemblyPackageDependency.value
        val conf = baseDirectory.value / "src/main/resources"
        val build = baseDirectory.value / "target/scala-2.11"
        IO.copyDirectory(conf, build)
        // IO.createDirectory( build / "logs")
}