package org.typelevel.jawn.fs2

import fs2.Chunk
import java.nio.ByteBuffer
import org.typelevel.jawn.{AsyncParser, Facade, ParseException}

/** Type class that can be absorbed by a Jawn AsyncParser
  */
trait Absorbable[A] {
  def absorb[J](parser: AsyncParser[J], a: A)(implicit
      rawFacade: Facade[J]): Either[ParseException, collection.Seq[J]]
}

object Absorbable {
  implicit val StringAbsorbable: Absorbable[String] = new Absorbable[String] {
    override def absorb[J](parser: AsyncParser[J], string: String)(implicit
        rawFacade: Facade[J]): Either[ParseException, collection.Seq[J]] =
      parser.absorb(string)
  }

  implicit val ByteBufferAbsorbable: Absorbable[ByteBuffer] = new Absorbable[ByteBuffer] {
    override def absorb[J](parser: AsyncParser[J], bytes: ByteBuffer)(implicit
        rawFacade: Facade[J]): Either[ParseException, collection.Seq[J]] =
      parser.absorb(bytes)
  }

  implicit val ByteArrayAbsorbable: Absorbable[Array[Byte]] = new Absorbable[Array[Byte]] {
    override def absorb[J](parser: AsyncParser[J], bytes: Array[Byte])(implicit
        rawFacade: Facade[J]): Either[ParseException, collection.Seq[J]] =
      parser.absorb(bytes)
  }

  implicit def ByteChunkAbsorbable[C <: Chunk[Byte]]: Absorbable[C] =
    new Absorbable[C] {
      override def absorb[J](parser: AsyncParser[J], chunk: C)(implicit
          rawFacade: Facade[J]): Either[ParseException, collection.Seq[J]] =
        parser.absorb(chunk.toArray)
    }
}
