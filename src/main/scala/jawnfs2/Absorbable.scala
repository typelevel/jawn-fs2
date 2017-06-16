package jawnfs2

import java.nio.ByteBuffer

import fs2.{Chunk, Segment}
import jawn.{AsyncParser, Facade, ParseException}

/**
  * Type class that can be absorbed by a Jawn AsyncParser
  */
trait Absorbable[A] {
  def absorb[J](parser: AsyncParser[J], a: A)(implicit facade: Facade[J]): Either[ParseException, Seq[J]]
}

object Absorbable {
  implicit val StringAbsorbable: Absorbable[String] = new Absorbable[String] {
    override def absorb[J](parser: AsyncParser[J], string: String)(
        implicit facade: Facade[J]): Either[ParseException, Seq[J]] = parser.absorb(string)
  }

  implicit val ByteBufferAbsorbable: Absorbable[ByteBuffer] = new Absorbable[ByteBuffer] {
    override def absorb[J](parser: AsyncParser[J], bytes: ByteBuffer)(
        implicit facade: Facade[J]): Either[ParseException, Seq[J]] = parser.absorb(bytes)
  }

  implicit val ByteArrayAbsorbable: Absorbable[Array[Byte]] = new Absorbable[Array[Byte]] {
    override def absorb[J](parser: AsyncParser[J], bytes: Array[Byte])(
        implicit facade: Facade[J]): Either[ParseException, Seq[J]] = parser.absorb(bytes)
  }

  implicit def ByteSegmentAbsorbable[S <: Segment[Byte, _]]: Absorbable[S] = new Absorbable[S] {
    override def absorb[J](parser: AsyncParser[J], segment: S)(
      implicit facade: Facade[J]): Either[ParseException, Seq[J]] = parser.absorb(segment.toChunk.toArray)
  }

  implicit def ByteChunkAbsorbable[C <: Chunk[Byte]]: Absorbable[C] = new Absorbable[C] {
    override def absorb[J](parser: AsyncParser[J], chunk: C)(
        implicit facade: Facade[J]): Either[ParseException, Seq[J]] = parser.absorb(chunk.toArray)
  }
}
