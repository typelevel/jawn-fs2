package jawnstreamz

import java.nio.ByteBuffer

import jawn.{Facade, AsyncParser}
import jawn.ast._
import org.specs2.execute.Result
import org.specs2.mutable.Specification
import scodec.bits.ByteVector
import scala.collection.mutable.Map
import scalaz.\/-
import scalaz.concurrent.Task
import scalaz.stream.{Step, Process, async, io}
import scalaz.stream.text.utf8Encode

class JawnStreamzSpec extends Specification {
  def loadJson(name: String, chunkSize: Int = 1024): Process[Task, ByteVector]  = {
    val is = getClass.getResourceAsStream(s"${name}.json")
    Process.constant(chunkSize).through(io.chunkR(is))
  }

  def taskSource[A](a: A*): Process[Task, A] = Process.apply(a: _*).asInstanceOf[Process[Task, A]]

  implicit val facade = JParser.facade

  "parseJson" should {
    def parse[A: Absorbable](a: A*): Option[JValue] =
      taskSource(a: _*).parseJson(AsyncParser.SingleValue).runLog.run.headOption

    "absorb strings" in {
      parse(""""string"""") must_== Some(JString("string"))
    }

    "absorb byte arrays" in {
      parse(""""byte array"""".getBytes("utf-8")) must_== Some(JString("byte array"))
    }

    "absorb byte buffers" in {
      parse(""""byte buffer"""") must_== Some(JString("byte buffer"))
    }

    "absorb byte vectors" in {
      parse(""""byte vector"""") must_== Some(JString("byte vector"))
    }

    "be reusable" in {
      val p = parseJson[ByteVector, JValue](AsyncParser.SingleValue)
      def runIt = loadJson("single").pipe(p).runLog.run
      runIt must_== runIt
    }
  }

  "runJson" should {
    "return a single JSON value" in {
      loadJson("single").runJson.run must_== JObject(Map("one" -> JNum(1L)))
    }

    "return a single JSON value from multiple chunks" in {
      loadJson("single", 1).runJson.run must_== JObject(Map("one" -> JNum(1L)))
    }

    "return JNull for empty source" in {
      taskSource(ByteVector.empty).runJson.run must_== JNull
    }
  }

  "parseJsonStream" should {
    "return a stream of JSON values" in {
      loadJson("stream").parseJsonStream.runLog.run must_== Vector(
        JObject(Map("one" -> JNum(1L))),
        JObject(Map("two" -> JNum(2L))),
        JObject(Map("three" -> JNum(3L)))
      )
    }
  }

  "unwrapJsonArray" should {
    "emit an array of JSON values asynchronously" in Result.unit {
      val (q, stringSource) = async.queue[String]
      val stream = stringSource.unwrapJsonArray
      q.enqueue("""[1,""")
      val Step(first, tail, cleanup) = stream.runStep.run
      first must_== \/-(Seq(JNum(1)))
      q.enqueue("""2,3]""")
      q.close
      tail.runLog.run must_== Vector(JNum(2), JNum(3))
    }
  }
}
