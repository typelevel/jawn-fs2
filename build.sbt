resolvers ++= Seq(
  "bintray/non" at "http://dl.bintray.com/non/maven",
  "bintray/scalaz" at "http://dl.bintray.com/scalaz/releases"
)

val JawnVersion = "0.5.4"

libraryDependencies ++= Seq(
  "org.jsawn" %% "jawn-ast" % JawnVersion,
  "org.jsawn" %% "jawn-parser" % JawnVersion,
  "org.scalaz.stream" %% "scalaz-stream" % "0.4.1",
  "org.specs2" %% "specs2" % "2.3.13" % "test"
)
