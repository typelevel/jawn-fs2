import java.net.URL

sonatypeSettings

organization := "org.http4s"

name := "jawn-fs2"

scalaVersion := "2.11.8"

version := s"0.9.0"

pomExtra := {
  <url>http://github.com/rossabaker/jawn-fs2</url>
  <scm>
    <connection>scm:git:github.com/rossabaker/jawn-fs2</connection>
    <developerConnection>scm:git:github.com/rossabaker/jawn-fs2</developerConnection>
    <url>github.com/rossabaker/jawn-fs2</url>
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
  "bintray/non" at "http://dl.bintray.com/non/maven"
)

scalacOptions ++= Seq(
  "-deprecation",
  "-feature"
)

val JawnVersion = "0.10.1"

val Fs2Version = "0.9.1"

libraryDependencies ++= Seq(
  "org.spire-math" %% "jawn-parser" % JawnVersion,
  "org.scodec"     %% "scodec-bits" % "1.1.1",
  "co.fs2"         %% "fs2-core"    % Fs2Version,
  "org.spire-math" %% "jawn-ast"    % JawnVersion % "test",
  "co.fs2"         %% "fs2-io"      % Fs2Version  % "test",
  "org.specs2"     %% "specs2-core" % "3.8.5"     % "test"
)
