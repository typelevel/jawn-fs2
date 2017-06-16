package jawnfs2.examples

import java.nio.file.Paths
import java.util.concurrent.Executors

import cats.effect._
import fs2.{io, text, time}
import jawnfs2._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Random.nextInt

object Example extends App {
  // Pick your favorite supported AST (e.g., json4s, argonaut, etc.)
  implicit val facade = jawn.ast.JawnFacade

  implicit val ec = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  implicit val scheduler = fs2.Scheduler.fromFixedDaemonPool(4)

  // From JSON on disk
  val jsonStream = io.file.readAll[IO](Paths.get("testdata/random.json"), 64)
  // Introduce up to a second of lag between chunks
  val laggedStream = jsonStream.chunks.zipWith(time.awakeEvery[IO](nextInt(1000).millis))((chunk, _) => chunk)
  // Print each element of the JSON array as we read it
  val json = laggedStream.unwrapJsonArray.map(_.toString).intersperse("\n").through(text.utf8Encode)
  // run converts the stream into an IO, unsafeRunSync executes the IO for its effects
  json.to(io.stdout).run.unsafeRunSync
}
