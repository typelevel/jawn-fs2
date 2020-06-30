organization := "org.http4s"
name := "jawn-fs2"

scalaVersion := "2.13.2"
crossScalaVersions := Seq("2.12.10", scalaVersion.value)

version := "1.0.0"

val JawnVersion   = "1.0.0"
val Fs2Version    = "2.3.0"
val Specs2Version = "4.10.0"

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
