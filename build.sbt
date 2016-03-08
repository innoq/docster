name := """docster"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

javaOptions in Test ++= Seq(s"-Dlogger.resource=" + sys.props.getOrElse("logger.resource", default = "logback.xml"))

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "com.github.tomakehurst" % "wiremock" % "1.58",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "org.mockito" % "mockito-all" % "1.10.19",
  "org.jsoup" % "jsoup" % "1.8.3",
  "com.theoryinpractise" % "halbuilder-standard" % "4.0.1"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
