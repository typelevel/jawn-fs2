import SonatypeKeys._

sonatypeSettings

organization := "org.http4s"

name := "jawn-streamz"

version := "0.3.1-SNAPSHOT"

scalaVersion := "2.10.4"

crossScalaVersions := Seq("2.10.4", "2.11.4")

pomExtra := {
  <url>http://github.com/rossabaker/jawn-streamz</url>
  <scm>
    <connection>scm:git:github.com/rossabaker/jawn-streamz</connection>
    <developerConnection>scm:git:github.com/rossabaker/jawn-streamz</developerConnection>
    <url>github.com/rossabaker/jawn-streamz</url>
  </scm>
  <developers>
    <id>rossabaker</id>
    <name>Ross A. Baker</name>
    <email>ross@rossabaker.com</email>
  </developers>
}

licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

resolvers ++= Seq(
  "bintray/non" at "http://dl.bintray.com/non/maven",
  "bintray/scalaz" at "http://dl.bintray.com/scalaz/releases"
)

scalacOptions ++= Seq(
  "-deprecation",
  "-feature"
)

val JawnVersion = "0.7.0"

libraryDependencies ++= Seq(
  "org.spire-math" %% "jawn-parser" % JawnVersion,
  "org.spire-math" %% "jawn-ast" % JawnVersion % "test",
  "org.scalaz.stream" %% "scalaz-stream" % "0.6a",
  "org.specs2" %% "specs2" % "2.4" % "test"
)
