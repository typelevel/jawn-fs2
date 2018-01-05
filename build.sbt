organization := "org.http4s"
name := "jawn-fs2"

scalaVersion := "2.12.4"
crossScalaVersions := Seq("2.11.12", "2.12.4")

version := s"0.12.0-M6"

val JawnVersion = "0.11.0"
val Fs2Version = "0.10.0-M11"

libraryDependencies ++= Seq(
  "org.spire-math" %% "jawn-parser" % JawnVersion,
  "co.fs2"         %% "fs2-core"    % Fs2Version,
  "org.spire-math" %% "jawn-ast"    % JawnVersion % "test",
  "co.fs2"         %% "fs2-io"      % Fs2Version  % "test",
  "org.specs2"     %% "specs2-core" % "4.0.2"     % "test"
)

publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)
