name := """my-first-app"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "com.github.seratch" %% "awscala" % "0.5.+",
  "org.scalaj" %% "scalaj-http" % "1.1.4",
  "com.github.nscala-time" %% "nscala-time" % "2.0.0",
  "org.apache.httpcomponents" % "httpclient" % "4.3.1", // added to solve run test issue in intellij
  "org.apache.httpcomponents" % "httpcore" % "4.3.1"    // added to solve run test issue in intellij
)

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.3.1"

libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "2.6.0"

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"

packAutoSettings