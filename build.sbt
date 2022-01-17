ThisBuild / organization := "org.http4s"

ThisBuild / crossScalaVersions := Seq("2.12.15", "3.0.2", "2.13.7")
ThisBuild / tlBaseVersion := "1.2"
ThisBuild / tlCiReleaseBranches := Seq("series/1.x")
startYear := Some(2014)

ThisBuild / developers := List(
  Developer(
    id = "rossabaker",
    name = "Ross A. Baker",
    email = "ross@rossabaker.com",
    url = url("https://github.com/rossabaker")
  ),
  Developer(
    id = "ChristopherDavenport",
    name = "Christopher Davenport",
    email = "chris@christopherdavenport.tech",
    url = url("https://github.com/ChristopherDavenport")
  )
)

val JawnVersion = "1.3.2"
val Fs2Version = "2.5.10"
val Specs2Version = "4.13.1"

libraryDependencies ++= Seq(
  "org.typelevel" %% "jawn-parser" % JawnVersion,
  "co.fs2" %% "fs2-core" % Fs2Version,
  "org.typelevel" %% "jawn-ast" % JawnVersion % Test,
  "co.fs2" %% "fs2-io" % Fs2Version % Test,
  "org.specs2" %% "specs2-core" % Specs2Version % Test cross CrossVersion.for3Use2_13
)

tlVersionIntroduced := Map("3" -> "1.1.3")
