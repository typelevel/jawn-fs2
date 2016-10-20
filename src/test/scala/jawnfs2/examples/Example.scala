package jawnfs2.examples

import java.nio.file.Paths

import fs2.{io, text, time, Task}
import jawnfs2._

import scala.concurrent.duration._
import scala.util.Random.nextInt

object Example extends App {
  // Pick your favorite supported AST (e.g., json4s, argonaut, etc.)
  implicit val facacde = jawn.ast.JawnFacade

  implicit val strategy  = fs2.Strategy.fromCachedDaemonPool()
  implicit val scheduler = fs2.Scheduler.fromFixedDaemonPool(4)

  // From JSON on disk
  val jsonStream = io.file.readAll[Task](Paths.get("testdata/random.json"), 64)
  // Introduce up to a second of lag between chunks
  val laggedStream = jsonStream.chunks.zipWith(time.awakeEvery[Task](nextInt(1000).millis))((chunk, _) => chunk)
  // Print each element of the JSON array as we read it
  val json = laggedStream.unwrapJsonArray.map(_.toString).intersperse("\n").through(text.utf8Encode)
  // run converts the stream into a Task, unsafeRun executes the task for its effects
  json.to(io.stdout).run.unsafeRun
}
