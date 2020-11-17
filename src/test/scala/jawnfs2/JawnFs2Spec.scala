package jawnfs2

import cats.effect.{Blocker, ContextShift, ExitCase, IO, Resource}
import fs2.io.file.readAll
import fs2.{Chunk, Stream}
import java.nio.ByteBuffer
import java.nio.file.Paths
import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragments
import org.typelevel.jawn.AsyncParser
import org.typelevel.jawn.ast._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global

class JawnFs2Spec extends Specification {
  def withResource[A](r: Resource[IO, A])(fs: A => Fragments): Fragments =
    r match {
      case Resource.Allocate(alloc) =>
        alloc
          .map {
            case (a, release) =>
              fs(a).append(step(release(ExitCase.Completed).unsafeRunSync()))
          }
          .unsafeRunSync()
      case Resource.Bind(r, f) =>
        withResource(r)(a => withResource(f(a))(fs))
      case Resource.Suspend(r) =>
        withResource(r.unsafeRunSync() /* ouch */ )(fs)
    }

  implicit val contextShift: ContextShift[IO] = IO.contextShift(global)

  def loadJson(name: String, blocker: Blocker, chunkSize: Int = 1024): Stream[IO, Chunk[Byte]] =
    readAll[IO](Paths.get(s"testdata/$name.json"), blocker, chunkSize).chunks

  implicit val facade = JParser.facade

  withResource(Blocker[IO]) { blocker =>
    "parseJson" should {
      def parse[A: Absorbable](a: A*): Option[JValue] =
        Stream(a: _*).covary[IO].parseJson(AsyncParser.SingleValue).compile.toVector.attempt.unsafeRunSync().fold(_ => None, _.headOption)

      "absorb strings" in {
        parse(""""string"""") must_== Some(JString("string"))
      }

      "absorb byte arrays" in {
        parse("""["byte array"]""".getBytes("utf-8")) must_== Some(JArray(Array(JString("byte array"))))
      }

      "absorb byte buffers" in {
        val buffer = ByteBuffer.wrap(""""byte buffer"""".getBytes("utf-8"))
        parse(buffer) must_== Some(JString("byte buffer"))
      }

      "include output from finish" in {
        parse("42") must_== Some(JNum(42))
      }

      "be reusable" in {
        val p     = parseJson[IO, Chunk[Byte], JValue](AsyncParser.SingleValue)
        def runIt = loadJson("single", blocker).through(p).compile.toVector.unsafeRunSync()
        runIt must_== runIt
      }
    }

    "runJsonOption" should {
      "return some single JSON value" in {
        loadJson("single", blocker).runJsonOption.unsafeRunSync() must_== Some(JObject(mutable.Map("one" -> JNum(1L))))
      }

      "return some single JSON value from multiple chunks" in {
        loadJson("single", blocker, 1).runJsonOption.unsafeRunSync() must_== Some(JObject(mutable.Map("one" -> JNum(1L))))
      }

      "return None for empty source" in {
        Stream(Array.empty[Byte]).covary[IO].runJsonOption.unsafeRunSync() must_== None
      }
    }

    "parseJsonStream" should {
      "return a stream of JSON values" in {
        loadJson("stream", blocker).parseJsonStream.compile.toVector.unsafeRunSync() must_== Vector(
          JObject(mutable.Map("one"   -> JNum(1L))),
          JObject(mutable.Map("two"   -> JNum(2L))),
          JObject(mutable.Map("three" -> JNum(3L)))
        )
      }
    }

    "unwrapJsonArray" should {
      "emit an array of JSON values asynchronously" in {
        Stream
          .eval(IO.pure("""[1,"""))
          .unwrapJsonArray
          .take(2)
          .compile
          .toVector
          .unsafeRunSync()
          .headOption
          .flatMap(_.getLong) must beSome(1L)
      }
    }
  }
}
