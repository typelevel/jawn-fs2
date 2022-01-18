ThisBuild / crossScalaVersions := Seq("2.12.15", "3.1.0", "2.13.8")
ThisBuild / tlBaseVersion := "2.2"
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

val JawnVersion = "1.3.2"
val Fs2Version = "3.2.4"
val MunitVersion = "0.7.29"
val MunitCatsEffectVersion = "1.0.7"

lazy val root = tlCrossRootProject.aggregate(`jawn-fs2`)

lazy val `jawn-fs2` = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
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
  .jsSettings(
    tlVersionIntroduced := List("2.12", "2.13", "3").map(_ -> "2.1.0").toMap,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )
