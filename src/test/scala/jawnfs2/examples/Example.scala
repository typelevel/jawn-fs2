package jawnfs2.examples

import cats.effect._
import cats.implicits._
import fs2.{Stream, io, text}
import java.nio.file.Paths
import jawnfs2._
import org.typelevel.jawn.ast.JawnFacade
import scala.concurrent.duration._

object Example extends IOApp {
  // Pick your favorite supported AST (e.g., json4s, argonaut, etc.)
  implicit val facade = JawnFacade

  def run(args: List[String]) =
    // Uses blocking IO, so provide an appropriate thread pool
    Blocker[IO].use { blocker =>
      // From JSON on disk
      val jsonStream = io.file.readAll[IO](Paths.get("testdata/random.json"), blocker, 64)
      // Simulate lag between chunks
      val lag = Stream.awakeEvery[IO](100.millis)
      val laggedStream = jsonStream.chunks.zipWith(lag)((chunk, _) => chunk)
      // Print each element of the JSON array as we read it
      val json = laggedStream.unwrapJsonArray.map(_.toString).intersperse("\n").through(text.utf8Encode)
      // run converts the stream into an IO, unsafeRunSync executes the IO for its effects
      json.through(io.stdout(blocker)).compile.drain.as(ExitCode.Success)
    }
}
