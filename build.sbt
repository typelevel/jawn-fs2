import java.net.URL

import SonatypeKeys._

sonatypeSettings

organization := "org.http4s"

name := "jawn-streamz"

scalaVersion := "2.10.6"

scalazVersion := "7.1.10"

crossScalaVersions := {
  Seq("2.10.6", "2.11.8", "2.12.0").filterNot { sv =>
    VersionNumber(scalazVersion.value).numbers match {
      case Seq(7, 1, _*) => sv.startsWith("2.12.0")
      case Seq(7, 2, _*) => false
    }
  }
}

val scalazVersion = settingKey[String]("The version of Scalaz used for building.")
def scalazStreamVersion(scalazVersion: String) =
  "0.8.6" + scalazCrossBuildSuffix(scalazVersion)
def scalazCrossBuildSuffix(scalazVersion: String) =
  VersionNumber(scalazVersion).numbers match {
    case Seq(7, 1, _*) => ""
    case Seq(7, 2, _*) => "a"
  }
def specs2Version(scalazVersion: String) =
  VersionNumber(scalazVersion).numbers match {
    case Seq(7, 1, _*) => "3.8.6-scalaz-7.1"
    case Seq(7, 2, _*) => "3.8.6"
  }

version := s"0.10.1${scalazCrossBuildSuffix(scalazVersion.value)}"

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

val JawnVersion = "0.10.3"

libraryDependencies ++= Seq(
  "org.spire-math" %% "jawn-parser" % JawnVersion,
  "org.spire-math" %% "jawn-ast" % JawnVersion % "test",
  "org.scalaz.stream" %% "scalaz-stream" % scalazStreamVersion(scalazVersion.value),
  "org.specs2" %% "specs2-core" % specs2Version(scalazVersion.value) % "test"
)
