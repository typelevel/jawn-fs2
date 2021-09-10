ThisBuild / organization := "org.typelevel"
ThisBuild / organizationName := "Typelevel"

ThisBuild / crossScalaVersions := Seq("2.12.14", "2.13.6", "3.0.2")
ThisBuild / scalaVersion := crossScalaVersions.value.filter(_.startsWith("2.")).last
ThisBuild / baseVersion := "2.0"
ThisBuild / publishGithubUser := "rossabaker"
ThisBuild / publishFullName := "Ross A. Baker"
ThisBuild / githubWorkflowTargetBranches := List("*", "series/*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v")))
ThisBuild / homepage := Some(url("https://github.com/typelevel/jawn-fs2"))
ThisBuild / scmInfo := Some(
  ScmInfo(url("https://github.com/typelevel/jawn-fs2"), "git@github.com:typelevel/jawn-fs2.git")
)

val JawnVersion = "1.2.0"
val Fs2Version = "3.1.2"
val MunitVersion = "0.7.29"
val MunitCatsEffectVersion = "1.0.5"

lazy val root = project
  .in(file("."))
  .settings(
    Compile / unmanagedSourceDirectories := Seq.empty,
    Test / unmanagedSourceDirectories := Seq.empty,
  )
  .enablePlugins(NoPublishPlugin)
  .aggregate(`jawn-fs2`.jvm, `jawn-fs2`.js)

lazy val `jawn-fs2` = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("."))
  .enablePlugins(SonatypeCiReleasePlugin)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "jawn-parser" % JawnVersion,
      "co.fs2" %%% "fs2-core" % Fs2Version,
      "co.fs2" %%% "fs2-io" % Fs2Version % Test,
      "org.typelevel" %%% "jawn-ast" % JawnVersion % Test,
      "org.scalameta" %%% "munit" % MunitVersion % Test,
      "org.typelevel" %%% "munit-cats-effect-3" % MunitCatsEffectVersion % Test
    )
  )
  .jsSettings(scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) })
