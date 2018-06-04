package jawnfs2.examples

import cats.effect._
import fs2.{Stream, io, text}
import java.nio.file.Paths
import jawnfs2._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Example extends App {
  // Pick your favorite supported AST (e.g., json4s, argonaut, etc.)
  implicit val facade = jawn.ast.JawnFacade

  // From JSON on disk
  val jsonStream = io.file.readAll[IO](Paths.get("testdata/random.json"), 64)
  // Introduce lag between chunks
  val lag = Stream.awakeEvery[IO](500.millis)
  val laggedStream = jsonStream.chunks.zipWith(lag)((chunk, _) => chunk)
  // Print each element of the JSON array as we read it
  val json = laggedStream.unwrapJsonArray.map(_.toString).intersperse("\n").through(text.utf8Encode)
  // run converts the stream into an IO, unsafeRunSync executes the IO for its effects
  json.to(io.stdout).compile.drain.unsafeRunSync
}
