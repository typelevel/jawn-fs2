# jawn-fs2 [![Build Status](https://travis-ci.org/http4s/jawn-fs2.svg?branch=master)](https://travis-ci.org/http4s/jawn-fs2) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.http4s/jawn-fs2_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.http4s/jawn-fs2_2.12)

Asynchronously parse [fs2](https://github.com/functional-streams-for-scala/fs2) streams
to JSON values with [jawn](https://github.com/non/jawn).

## Example

`sbt test:run` to see it in action:

```Scala
import jawnfs2._

object Example extends App {
  // Pick your favorite supported AST (e.g., json4s, argonaut, etc.)
  implicit val facade = jawn.ast.JawnFacade
  // From JSON on disk
  val jsonStream = io.file.readAll[Task](Paths.get("testdata/random.json"), 64)
  // Introduce up to a second of lag between chunks
  val laggedStream = jsonStream.chunks.zipWith(time.awakeEvery[Task](nextInt(1000).millis))((chunk, _) => chunk)
  // Print each element of the JSON array as we read it
  val json = laggedStream.unwrapJsonArray.map(_.toString).intersperse("\n").through(text.utf8Encode)
  // run converts the stream into a Task, unsafeRun executes the task for its effects
  json.to(io.stdout).run.unsafeRun
}
```

## Add jawn-fs2 to your project

Add to your build.sbt:

```
libraryDependencies += "org.http4s" %% "jawn-fs2" % "0.12.2"

// Pick your AST: https://github.com/non/jawn#supporting-external-asts-with-jawn
libraryDependencies += "org.spire-math" %% "jawn-ast" % "0.12.2"
```

## Compatibility matrix

| Stream Library      | You need...                                  | Status
| ------------------- | -------------------------------------------- | ------
| fs2-0.10.x          | `"org.http4s" %% "jawn-fs2" % "0.12.2"`      | stable
| fs2-0.9.x           | `"org.http4s" %% "jawn-fs2" % "0.10.1"`      | EOL
| scalaz-stream-0.8a  | `"org.http4s" %% "jawn-streamz" % "0.10.1a"` | EOL
| scalaz-stream-0.8.x | `"org.http4s" %% "jawn-streamz" % "0.10.1"`  | EOL

The legacy scalaz-stream artifacts are on the [jawn-streamz](https://github.com/rossabaker/jawn-fs2/tree/jawn-streamz) branch.
