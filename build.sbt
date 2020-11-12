organization := "org.http4s"
name := "jawn-fs2"

scalaVersion := "2.13.3"
crossScalaVersions := Seq("2.12.12", scalaVersion.value)

version := "1.0.0"

val JawnVersion   = "1.0.1"
val Fs2Version    = "3.0.0-M1"
val Specs2Version = "4.10.5"

libraryDependencies ++= Seq(
  "org.typelevel"  %% "jawn-parser" % JawnVersion,
  "co.fs2"         %% "fs2-core"    % Fs2Version,
  "org.typelevel"  %% "jawn-ast"    % JawnVersion   % "test",
  "co.fs2"         %% "fs2-io"      % Fs2Version    % "test",
  "org.specs2"     %% "specs2-core" % Specs2Version % "test",
)

publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)
