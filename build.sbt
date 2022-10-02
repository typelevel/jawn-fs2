ThisBuild / crossScalaVersions := Seq("2.12.16", "3.1.3", "2.13.8")
ThisBuild / tlBaseVersion := "2.3"
ThisBuild / tlVersionIntroduced := Map("3" -> "2.0.2")
ThisBuild / startYear := Some(2014)

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

val JawnVersion = "1.4.0"
val Fs2Version = "3.3.0"
val MunitVersion = "1.0.0-M6"
val MunitCatsEffectVersion = "2.0.0-M3"

lazy val root = tlCrossRootProject.aggregate(`jawn-fs2`)

lazy val `jawn-fs2` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "jawn-parser" % JawnVersion,
      "co.fs2" %%% "fs2-core" % Fs2Version,
      "co.fs2" %%% "fs2-io" % Fs2Version % Test,
      "org.typelevel" %%% "jawn-ast" % JawnVersion % Test,
      "org.scalameta" %%% "munit" % MunitVersion % Test,
      "org.typelevel" %%% "munit-cats-effect" % MunitCatsEffectVersion % Test
    )
  )
  .jsSettings(
    tlVersionIntroduced := List("2.12", "2.13", "3").map(_ -> "2.1.0").toMap,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )
  .nativeSettings(
    tlVersionIntroduced := List("2.12", "2.13", "3").map(_ -> "2.2.1").toMap
  )
