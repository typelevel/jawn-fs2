package org.typelevel.jawn.fs2.examples

import cats.effect._
import fs2.io.file.Files
import fs2.{Stream, io, text}
import java.nio.file.Paths
import org.typelevel.jawn.ast.JawnFacade
import org.typelevel.jawn.fs2._
import scala.concurrent.duration._

object Example extends IOApp {
  // Pick your favorite supported AST (e.g., json4s, argonaut, etc.)
  implicit val facade: JawnFacade.type = JawnFacade

  def run(args: List[String]): IO[ExitCode] = {
    // From JSON on disk
    val jsonStream = Files[IO].readAll(Paths.get("testdata/random.json"), 64)
    // Simulate lag between chunks
    val lag = Stream.awakeEvery[IO](100.millis)
    val laggedStream = jsonStream.chunks.zipWith(lag)((chunk, _) => chunk)
    // Print each element of the JSON array as we read it
    val json =
      laggedStream.unwrapJsonArray.map(_.toString).intersperse("\n").through(text.utf8Encode)
    // run converts the stream into an IO, unsafeRunSync executes the IO for its effects
    json.through(io.stdout).compile.drain.as(ExitCode.Success)
  }
}
