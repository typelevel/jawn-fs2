package jawnfs2

import java.nio.ByteBuffer

import fs2.{Chunk, NonEmptyChunk}
import jawn.{AsyncParser, Facade, ParseException}
import scodec.bits.ByteVector

/**
  * Type class that can be absorbed by a Jawn AsyncParser
  */
trait Absorbable[A] {
  def absorb[J](parser: AsyncParser[J], a: A)(implicit facade: Facade[J]): Either[ParseException, Seq[J]]
}

object Absorbable {
  implicit val ByteBufferAbsorbable: Absorbable[ByteBuffer] = new Absorbable[ByteBuffer] {
    override def absorb[J](parser: AsyncParser[J], bytes: ByteBuffer)(
        implicit facade: Facade[J]): Either[ParseException, Seq[J]] = parser.absorb(bytes)
  }

  implicit val StringAbsorbable: Absorbable[String] = new Absorbable[String] {
    override def absorb[J](parser: AsyncParser[J], string: String)(
        implicit facade: Facade[J]): Either[ParseException, Seq[J]] = parser.absorb(string)
  }

  implicit val ByteArrayAbsorbable: Absorbable[Array[Byte]] = new Absorbable[Array[Byte]] {
    override def absorb[J](parser: AsyncParser[J], bytes: Array[Byte])(
        implicit facade: Facade[J]): Either[ParseException, Seq[J]] = parser.absorb(bytes)
  }

  implicit val ByteVectorAbsorbable: Absorbable[ByteVector] = new Absorbable[ByteVector] {
    override def absorb[J](parser: AsyncParser[J], bytes: ByteVector)(
        implicit facade: Facade[J]): Either[ParseException, Seq[J]] =
      parser.absorb(bytes.toByteBuffer)
  }

  implicit val ByteChunkAbsorbable: Absorbable[NonEmptyChunk[Byte]] = new Absorbable[NonEmptyChunk[Byte]] {
    override def absorb[J](parser: AsyncParser[J], chunk: NonEmptyChunk[Byte])(
        implicit facade: Facade[J]): Either[ParseException, Seq[J]] =
      parser.absorb(chunk.toArray)
  }
}
