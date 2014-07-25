seq(bintrayPublishSettings: _*)

organization := "com.rossabaker"

name := "jawn-streamz"

version := "0.2.0"

scalaVersion := "2.10.4"

crossScalaVersions := Seq("2.10.4", "2.11.1")

licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

resolvers ++= Seq(
  "bintray/non" at "http://dl.bintray.com/non/maven",
  "bintray/scalaz" at "http://dl.bintray.com/scalaz/releases"
)

val JawnVersion = "0.5.4"

libraryDependencies ++= Seq(
  "org.jsawn" %% "jawn-parser" % JawnVersion,
  "org.scalaz.stream" %% "scalaz-stream" % "0.4.1",
  "org.specs2" %% "specs2" % "2.3.13" % "test",
  "org.jsawn" %% "jawn-ast" % JawnVersion % "test"
)
