package jawnstreamz.examples

import scala.concurrent.duration._
import scala.util.Random.nextInt
import scalaz.concurrent.Task
import scalaz.stream.{Process, io}
import scalaz.stream.Process._
import scalaz.stream.time.awakeEvery
import jawnstreamz._

object Example extends App {
  // Pick your favorite supported AST (e.g., json4s, argonaut, etc.)
  implicit val facacde = jawn.ast.JawnFacade

  implicit val scheduler = scalaz.stream.DefaultScheduler

  // Read up to 64 bytes at a time
  val chunkSizes: Process[Task, Int] = emitAll(Stream.continually(nextInt(64)))
  // From JSON on disk
  val jsonSource = chunkSizes.through(io.chunkR(getClass.getResourceAsStream("/jawnstreamz/random.json")))
  // Introduce up to a second of lag between chunks
  val laggedSource = jsonSource.zipWith(awakeEvery(nextInt(1000).millis))((chunk, _) => chunk)
  // Print each element of the JSON array as we read it
  val json = laggedSource.unwrapJsonArray.map(_.toString()).to(io.stdOutLines)
  // run converts process into a Task, unsafePerformSync executes the task for its effects
  json.run.unsafePerformSync
}
