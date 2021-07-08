/*
 * Copyright 2021 Typelevel
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
import fs2.Chunk
import fs2.Stream
import fs2.io.file.Files

import java.nio.file.Paths

private[fs2] trait JawnFs2SuitePlatform {
  private[fs2] def loadJson(name: String, chunkSize: Int = 1024): Stream[IO, Chunk[Byte]] =
    Files[IO].readAll(Paths.get(s"testdata/$name.json"), chunkSize).chunks
}
