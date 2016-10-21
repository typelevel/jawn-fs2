# jawn-fs2

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
resolvers += "bintray/rossabaker" at "http://dl.bintray.com/rossabaker/maven"

libraryDependencies += "org.http4s" %% "jawn-fs2" % "0.9.0"

// Pick your AST: https://github.com/non/jawn#supporting-external-asts-with-jawn
libraryDependencies += "org.jsawn" %% "jawn-ast" % "0.10.1"
```
