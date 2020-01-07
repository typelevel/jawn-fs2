organization := "org.http4s"
name := "jawn-fs2"

scalaVersion := "2.12.8"
crossScalaVersions := Seq("2.11.12", scalaVersion.value, "2.13.0")

version := "0.15.0"

val JawnVersion   = "0.14.3"
val Fs2Version    = "2.1.0"
val Specs2Version = "4.8.2"

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
