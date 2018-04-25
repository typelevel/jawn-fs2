package jawnfs2

import java.nio.ByteBuffer
import java.nio.file.Paths

import cats.effect._
import fs2.{Segment, Stream, io}
import jawn.AsyncParser
import jawn.ast._
import org.specs2.mutable.Specification

import scala.collection.mutable

class JawnFs2Spec extends Specification {
  def loadJson(name: String, chunkSize: Int = 1024): Stream[IO, Segment[Byte, Unit]] =
    io.file.readAll[IO](Paths.get(s"testdata/$name.json"), chunkSize).segments

  implicit val facade = JParser.facade

  "parseJson" should {
    def parse[A: Absorbable](a: A*): Option[JValue] =
      Stream(a: _*).covary[IO].parseJson(AsyncParser.SingleValue).compile.toVector.attempt.unsafeRunSync.fold(_ => None, _.headOption)

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
      val p     = parseJson[IO, Segment[Byte, Unit], JValue](AsyncParser.SingleValue)
      def runIt = loadJson("single").through(p).compile.toVector.unsafeRunSync
      runIt must_== runIt
    }
  }

  "runJsonOption" should {
    "return some single JSON value" in {
      loadJson("single").runJsonOption.unsafeRunSync must_== Some(JObject(mutable.Map("one" -> JNum(1L))))
    }

    "return some single JSON value from multiple chunks" in {
      loadJson("single", 1).runJsonOption.unsafeRunSync must_== Some(JObject(mutable.Map("one" -> JNum(1L))))
    }

    "return None for empty source" in {
      Stream(Array.empty[Byte]).covary[IO].runJsonOption.unsafeRunSync must_== None
    }
  }

  "runJson" should {
    "return a single JSON value" in {
      loadJson("single").runJson.unsafeRunSync must_== JObject(mutable.Map("one" -> JNum(1L)))
    }

    "return a single JSON value from multiple chunks" in {
      loadJson("single", 1).runJson.unsafeRunSync must_== JObject(mutable.Map("one" -> JNum(1L)))
    }

    "return JNull for empty source" in {
      Stream(Array.empty[Byte]).covary[IO].runJson.unsafeRunSync must_== JNull
    }
  }

  "parseJsonStream" should {
    "return a stream of JSON values" in {
      loadJson("stream").parseJsonStream.compile.toVector.unsafeRunSync must_== Vector(
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
