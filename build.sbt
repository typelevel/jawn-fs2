import java.net.URL

import SonatypeKeys._

sonatypeSettings

organization := "org.http4s"

name := "jawn-streamz"

version := "0.7.0-SNAPSHOT"

scalaVersion := "2.10.5"

crossScalaVersions := Seq("2.10.5", "2.11.7")

pomExtra := {
  <url>http://github.com/rossabaker/jawn-streamz</url>
  <scm>
    <connection>scm:git:github.com/rossabaker/jawn-streamz</connection>
    <developerConnection>scm:git:github.com/rossabaker/jawn-streamz</developerConnection>
    <url>github.com/rossabaker/jawn-streamz</url>
  </scm>
}

developers := List(
  Developer(
    id = "rossabaker",
    name = "Ross A. Baker",
    email = "ross@rossabaker.com",
    url = new URL("https://github.com/rossabaker")
  )
)

licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

resolvers ++= Seq(
  "bintray/non" at "http://dl.bintray.com/non/maven",
  "bintray/scalaz" at "http://dl.bintray.com/scalaz/releases"
)

scalacOptions ++= Seq(
  "-deprecation",
  "-feature"
)

val JawnVersion = "0.8.3"

libraryDependencies ++= Seq(
  "org.spire-math" %% "jawn-parser" % JawnVersion,
  "org.spire-math" %% "jawn-ast" % JawnVersion % "test",
  "org.scalaz.stream" %% "scalaz-stream" % "0.7.3a",
  "org.specs2" %% "specs2-core" % "3.6.4" % "test"
)
