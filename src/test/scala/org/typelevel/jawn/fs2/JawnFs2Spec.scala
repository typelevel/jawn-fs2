/*
 * Copyright 2014 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.jawn.fs2

import cats.effect.unsafe.IORuntime
import cats.effect.{IO, unsafe}
import fs2.io.file.Files
import fs2.{Chunk, Stream}
import java.nio.ByteBuffer
import java.nio.file.Paths
import org.specs2.mutable.Specification
import org.typelevel.jawn.ast._
import org.typelevel.jawn.{AsyncParser, Facade}
import scala.collection.mutable

class JawnFs2Spec extends Specification {
  implicit val runtime: IORuntime = unsafe.IORuntime.global

  def loadJson(name: String, chunkSize: Int = 1024): Stream[IO, Chunk[Byte]] =
    Files[IO].readAll(Paths.get(s"testdata/$name.json"), chunkSize).chunks

  implicit val facade: Facade[JValue] = JParser.facade

  "parseJson" should {
    def parse[A: Absorbable](a: A*): Option[JValue] =
      Stream(a: _*)
        .covary[IO]
        .parseJson(AsyncParser.SingleValue)
        .compile
        .toVector
        .attempt
        .unsafeRunSync()
        .fold(_ => None, _.headOption)

    "absorb strings" in {
      parse(""""string"""") must beSome(JString("string"))
    }

    "absorb byte arrays" in {
      parse("""["byte array"]""".getBytes("utf-8")) must beSome(
        JArray(Array(JString("byte array"))))
    }

    "absorb byte buffers" in {
      val buffer = ByteBuffer.wrap(""""byte buffer"""".getBytes("utf-8"))
      parse(buffer) must beSome(JString("byte buffer"))
    }

    "include output from finish" in {
      parse("42") must beSome(JNum(42))
    }

    "be reusable" in {
      val p = parseJson[IO, Chunk[Byte], JValue](AsyncParser.SingleValue)

      def runIt = loadJson("single").through(p).compile.toVector.unsafeRunSync()

      runIt must_== runIt
    }
  }

  "runJsonOption" should {
    "return some single JSON value" in {
      loadJson("single").runJsonOption.unsafeRunSync() must beSome(
        JObject(mutable.Map("one" -> JNum(1L))))
    }

    "return some single JSON value from multiple chunks" in {
      loadJson("single", 1).runJsonOption.unsafeRunSync() must beSome(
        JObject(mutable.Map("one" -> JNum(1L))))
    }

    "return None for empty source" in {
      Stream(Array.empty[Byte]).covary[IO].runJsonOption.unsafeRunSync() must beNone
    }
  }

  "parseJsonStream" should {
    "return a stream of JSON values" in {
      loadJson("stream").parseJsonStream.compile.toVector.unsafeRunSync() must_== Vector(
        JObject(mutable.Map("one" -> JNum(1L))),
        JObject(mutable.Map("two" -> JNum(2L))),
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
