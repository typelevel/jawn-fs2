// To perform a release you will need your credentials present
// in $HOME/.sbt/1.0/sonatype.sbt in the form of
//
//   credentials += Credentials("Sonatype Nexus Repository Manager",
//     "oss.sonatype.org",
//     "(Sonatype user name)",
//     "(Sonatype password)")
//
// Then to perform the release
// sbt publishSigned
// sbt sonatypeRelease

sonatypeProfileName := organization.value

// To sync with Maven central
publishMavenStyle := true

licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/typelevel/jawn-fs2"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/typelevel/jawn-fs2"),
    "scm:git@github.com:typelevel/jawn-fs2.git"
  )
)
startYear := Some(2014)

developers := List(
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
