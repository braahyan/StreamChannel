name := """my-first-app"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.apache.httpcomponents" % "httpclient" % "4.3.1", // added to solve run test issue in intellij
  "org.apache.httpcomponents" % "httpcore" % "4.3.1"    // added to solve run test issue in intellij
)



javaOptions in Test += "-Dconfig.file=conf/application.test.conf"