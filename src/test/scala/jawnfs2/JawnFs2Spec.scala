package jawnfs2

import java.nio.ByteBuffer

import fs2.{Stream, Task, io}
import jawn.AsyncParser
import jawn.ast._
import org.specs2.mutable.Specification
import scodec.bits.ByteVector

import scala.collection.mutable

class JawnFs2Spec extends Specification {
  def loadJson(name: String, chunkSize: Int = 1024): Stream[Task, Array[Byte]] = {
    val is = Task.now(getClass.getResourceAsStream(s"$name.json"))
    io.readInputStream(is, chunkSize).chunks.map(_.toArray)
  }

  implicit val facade = JParser.facade

  "parseJson" should {
    def parse[A: Absorbable](a: A*): Option[JValue] =
      Stream(a: _*).parseJson(AsyncParser.SingleValue).runLog.fold(_ => None, _.headOption)

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

    "absorb byte vectors" in {
      val vector = ByteVector(""""byte vector"""".getBytes("utf-8"))
      parse(vector) must_== Some(JString("byte vector"))
    }

    "include output from finish" in {
      parse("42") must_== Some(JNum(42))
    }

    "be reusable" in {
      val p     = parseJson[Task, Array[Byte], JValue](AsyncParser.SingleValue)
      def runIt = loadJson("single").through(p).runLog.unsafeRun
      runIt must_== runIt
    }
  }

  "runJson" should {
    "return a single JSON value" in {
      loadJson("single").runJson.unsafeRun must_== JObject(mutable.Map("one" -> JNum(1L)))
    }

    "return a single JSON value from multiple chunks" in {
      loadJson("single", 1).runJson.unsafeRun must_== JObject(mutable.Map("one" -> JNum(1L)))
    }

    "return JNull for empty source" in {
      Stream[Task, ByteVector](ByteVector.empty).runJson.unsafeRun must_== JNull
    }
  }

  "parseJsonStream" should {
    "return a stream of JSON values" in {
      loadJson("stream").parseJsonStream.runLog.unsafeRun must_== Vector(
        JObject(mutable.Map("one"   -> JNum(1L))),
        JObject(mutable.Map("two"   -> JNum(2L))),
        JObject(mutable.Map("three" -> JNum(3L)))
      )
    }
  }
}
