# jawn-streamz

Asynchronously parse [scalaz-streams](http4s://github.com/scalaz/scalaz-stream)
to JSON values with [jawn](https://github.com/non/jawn).

## Example

`sbt test:run` to see it in action:

```Scala
import jawnstreamz._

object Example extends App {
  // Pick your favorite supported AST (e.g., json4s, argonaut, etc.)
  implicit val facade = jawn.ast.JawnFacade
  // Read up to 64 bytes at a time
  val chunkSizes: Process[Task, Int] = emitAll(Stream.continually(nextInt(64)))
  // From JSON on disk
  val jsonSource = chunkSizes.through(io.chunkR(getClass.getResourceAsStream("/jawnstreamz/random.json")))
  // Introduce up to a second of lag between chunks
  val laggedSource = jsonSource.zipWith(awakeEvery(nextInt(1000).millis))((chunk, _) => chunk)
  // Print each element of the JSON array as we read it
  val json = laggedSource.unwrapJsonArray.map(_.toString()).to(io.stdOutLines)
  // First run converts process into a Task, second run executes the task for its effects
  json.run.run
}
```

## Add jawn-streamz to your project

Add to your build.sbt:

```
resolvers += "bintray/rossabaker" at "http://dl.bintray.com/rossabaker/maven"

libraryDependencies += "org.http4s" %% "jawn-streamz" % "0.8.1"

// Pick your AST: https://github.com/non/jawn#supporting-external-asts-with-jawn
libraryDependencies += "org.jsawn" %% "jawn-ast" % "0.8.4"
```

## Compatibility

* scalaz-stream-0.8a -> jawn-streamz-0.8.1
* scalaz-stream-0.8 -> jawn-streamz-0.7.1
