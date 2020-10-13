# jawn-fs2 [![Build Status](https://travis-ci.org/typelevel/jawn-fs2.svg?branch=master)](https://travis-ci.org/typelevel/jawn-fs2) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.http4s/jawn-fs2_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.http4s/jawn-fs2_2.12)

Asynchronously parse [fs2](https://github.com/functional-streams-for-scala/fs2) streams
to JSON values with [jawn](https://github.com/non/jawn).

## Example

`sbt test:run` to see it in action:

```Scala
package jawnfs2.examples

import cats.effect.{IO, IOApp, ExitCode}
import fs2.{io, text, Stream}
import fs2.io.file.Files
import java.nio.file.Paths
import jawnfs2._
import scala.concurrent.duration._

object Example extends IOApp {
  // Pick your favorite supported AST (e.g., json4s, argonaut, etc.)
  implicit val facade = org.typelevel.jawn.ast.JawnFacade

  def run(args: List[String]) = {
    // From JSON on disk
    val jsonStream = Files[IO].readAll(Paths.get("testdata/random.json"), 64)
    // Simulate lag between chunks
    val lag = Stream.awakeEvery[IO](100.millis)
    val laggedStream = jsonStream.chunks.zipWith(lag)((chunk, _) => chunk)
    // Print each element of the JSON array as we read it
    val json = laggedStream.unwrapJsonArray
      .map(_.toString)
      .intersperse("\n")
      .through(text.utf8Encode)
    // run converts the stream into an IO, unsafeRunSync executes the IO for its effects
    json.through(io.stdout).compile.drain.as(ExitCode.Success)
  }
}
```

## Add jawn-fs2 to your project

Add to your build.sbt:

```
libraryDependencies += "org.http4s" %% "jawn-fs2" % "1.0.0"

// Pick your AST: https://github.com/non/jawn#supporting-external-asts-with-jawn
libraryDependencies += "org.typelevel" %% "jawn-ast" % "1.0.0"
```

## Compatibility matrix

| Stream Library      | You need...                                  | Status
| ------------------- | -------------------------------------------- | ------
| fs2-3.x             | `"org.http4s" %% "jawn-fs2" % "2.x"`         | development
| fs2-2.x             | `"org.http4s" %% "jawn-fs2" % "1.0.0"`       | stable
| fs2-1.x             | `"org.http4s" %% "jawn-fs2" % "0.14.2"`      | stable
| fs2-0.10.x          | `"org.http4s" %% "jawn-fs2" % "0.12.2"`      | EOL
| fs2-0.9.x           | `"org.http4s" %% "jawn-fs2" % "0.10.1"`      | EOL
| scalaz-stream-0.8a  | `"org.http4s" %% "jawn-streamz" % "0.10.1a"` | EOL
| scalaz-stream-0.8.x | `"org.http4s" %% "jawn-streamz" % "0.10.1"`  | EOL

The legacy scalaz-stream artifacts are on the [jawn-streamz](https://github.com/rossabaker/jawn-fs2/tree/jawn-streamz) branch.
