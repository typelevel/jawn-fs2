package jawnfs2

import java.nio.ByteBuffer

import fs2.{Stream, Task, io}
import jawn.AsyncParser
import jawn.ast._
import org.specs2.mutable.Specification
import scodec.bits.ByteVector

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
      val p = parseJson[Task, Array[Byte], JValue](AsyncParser.SingleValue)
      def runIt = loadJson("single").through(p).runLog.unsafeRun()
      runIt must_== runIt
    }
  }
}
