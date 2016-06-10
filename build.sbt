import java.net.URL

import SonatypeKeys._

sonatypeSettings

organization := "org.http4s"

name := "jawn-streamz"

scalaVersion := "2.10.6"

crossScalaVersions := Seq("2.10.6", "2.11.7")

val scalazVersion = settingKey[String]("The version of Scalaz used for building.")
def scalazStreamVersion(scalazVersion: String) =
  "0.8.2" + scalazCrossBuildSuffix(scalazVersion)
def scalazCrossBuildSuffix(scalazVersion: String) =
  VersionNumber(scalazVersion).numbers match {
    case Seq(7, 1, _*) => ""
    case Seq(7, 2, _*) => "a"
  }
def specs2Version(scalazVersion: String) =
  VersionNumber(scalazVersion).numbers match {
    case Seq(7, 1, _*) => "3.7.2-scalaz-7.1.7"
    case Seq(7, 2, _*) => "3.7.2"
  }

scalazVersion := "7.1.7"

version := s"0.9.0${scalazCrossBuildSuffix(scalazVersion.value)}-SNAPSHOT"

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

val JawnVersion = "0.8.4"

libraryDependencies ++= Seq(
  "org.spire-math" %% "jawn-parser" % JawnVersion,
  "org.spire-math" %% "jawn-ast" % JawnVersion % "test",
  "org.scalaz.stream" %% "scalaz-stream" % scalazStreamVersion(scalazVersion.value),
  "org.specs2" %% "specs2-core" % specs2Version(scalazVersion.value) % "test"
)
