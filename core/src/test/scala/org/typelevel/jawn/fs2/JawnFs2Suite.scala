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

import cats.effect.IO
import cats.effect.SyncIO
import cats.effect.kernel.Resource
import cats.syntax.parallel._
import fs2.Chunk
import fs2.Stream
import fs2.io.file.Files
import fs2.io.file.Flags
import fs2.io.file.Path
import munit.CatsEffectSuite
import org.typelevel.jawn.AsyncParser
import org.typelevel.jawn.Facade
import org.typelevel.jawn.ast._

import java.nio.ByteBuffer
import scala.collection.mutable

class JawnFs2Suite extends CatsEffectSuite {
  private def loadJson(name: String, chunkSize: Int = 1024): Stream[IO, Chunk[Byte]] =
    Files[IO].readAll(Path(s"testdata/$name.json"), chunkSize, Flags.Read).chunks

  implicit val facade: Facade[JValue] = JParser.facade

  private def liftResource[A](io: IO[A]): SyncIO[FunFixture[A]] =
    ResourceFixture(Resource.eval(io))

  private def parse[A: Absorbable](a: A*): SyncIO[FunFixture[Option[JValue]]] =
    liftResource(
      Stream(a: _*)
        .covary[IO]
        .parseJson(AsyncParser.SingleValue)
        .compile
        .toVector
        .attempt
        .map(_.fold(_ => None, _.headOption))
    )

  private def parseSuite[A: Absorbable](name: String, data: A, expectedValue: JValue): Unit =
    parse(data).test(name) {
      case Some(value) =>
        assertEquals(value, expectedValue)

      case None =>
        fail(name)
    }

  parseSuite(
    name = "parseJson should absorb strings",
    data = """"string"""",
    expectedValue = JString("string")
  )

  parseSuite(
    name = "parseJson should absorb byte arrays",
    data = """["byte array"]""".getBytes("utf-8"),
    expectedValue = JArray(Array(JString("byte array")))
  )

  parseSuite(
    name = "parseJson should absorb byte buffers",
    data = ByteBuffer.wrap(""""byte buffer"""".getBytes("utf-8")),
    expectedValue = JString("byte buffer")
  )

  parseSuite(
    name = "parseJson should include output from finish",
    data = "42",
    expectedValue = JNum(42)
  )

  private def parseJsonReusableTestCase = {
    val pipe = parseJson[IO, Chunk[Byte], JValue](AsyncParser.SingleValue)
    def runIt = loadJson("single").through(pipe).compile.toVector

    liftResource((runIt, runIt).parMapN(_ -> _))
  }

  parseJsonReusableTestCase.test("parseJson should be reusable") { case (firstRun, secondRun) =>
    assertEquals(firstRun, secondRun)
  }

  liftResource(loadJson("single").runJsonOption).test(
    "runJsonOption should return some single JSON value"
  ) {
    case Some(value) =>
      val expected = JObject(mutable.Map("one" -> JNum(1L)))
      assertEquals(value, expected)

    case None =>
      fail("runJsonOption should return some single JSON value")
  }

  liftResource(loadJson("single", 1).runJsonOption).test(
    "runJsonOption should return some single JSON value from multiple chunks"
  ) {
    case Some(value) =>
      val expected = JObject(mutable.Map("one" -> JNum(1L)))
      assertEquals(value, expected)

    case None =>
      fail("runJsonOption should return None for empty source")
  }

  private val emptyStream = Stream(Array.empty[Byte]).covary[IO]

  liftResource(emptyStream.runJsonOption).test(
    "runJsonOption should return None for empty source"
  ) {
    case None => ()

    case Some(_) =>
      fail("runJsonOption should return None for empty source")
  }

  private val jsons = loadJson("stream").parseJsonStream.compile.toVector

  liftResource(jsons).test(
    "parseJsonStream should return a stream of JSON values"
  ) { values =>
    val expectedValue = Vector(
      JObject(mutable.Map("one" -> JNum(1L))),
      JObject(mutable.Map("two" -> JNum(2L))),
      JObject(mutable.Map("three" -> JNum(3L)))
    )

    assertEquals(values, expectedValue)
  }

  private val dataForUnwrapJsonArrayTestCase =
    Stream
      .eval(IO.pure("""[1,"""))
      .unwrapJsonArray
      .take(2)
      .compile
      .toVector
      .map(_.headOption.flatMap(_.getLong))

  liftResource(dataForUnwrapJsonArrayTestCase).test(
    "unwrapJsonArray should emit an array of JSON values asynchronously"
  ) {
    case Some(value) =>
      assertEquals(value, 1L)

    case None =>
      fail("unwrapJsonArray should emit an array of JSON values asynchronously")
  }
}
