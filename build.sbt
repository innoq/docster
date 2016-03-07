name := """docster"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

javaOptions in Test ++= Seq(s"-Dlogger.resource=" + sys.props.getOrElse("logger.resource", default = ""))

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "com.github.tomakehurst" % "wiremock" % "1.58",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "org.mockito" % "mockito-all" % "1.10.19",
  "org.jsoup" % "jsoup" % "1.8.3"
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
