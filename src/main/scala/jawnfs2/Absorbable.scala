package jawnfs2

import fs2.{Chunk, Segment}
import java.nio.ByteBuffer
import jawn.{AsyncParser, ParseException, RawFacade}

/**
  * Type class that can be absorbed by a Jawn AsyncParser
  */
trait Absorbable[A] {
  def absorb[J](parser: AsyncParser[J], a: A)(implicit rawFacade: RawFacade[J]): Either[ParseException, Seq[J]]
}

object Absorbable {
  implicit val StringAbsorbable: Absorbable[String] = new Absorbable[String] {
    override def absorb[J](parser: AsyncParser[J], string: String)(
        implicit rawFacade: RawFacade[J]): Either[ParseException, Seq[J]] = parser.absorb(string)
  }

  implicit val ByteBufferAbsorbable: Absorbable[ByteBuffer] = new Absorbable[ByteBuffer] {
    override def absorb[J](parser: AsyncParser[J], bytes: ByteBuffer)(
        implicit rawFacade: RawFacade[J]): Either[ParseException, Seq[J]] = parser.absorb(bytes)
  }

  implicit val ByteArrayAbsorbable: Absorbable[Array[Byte]] = new Absorbable[Array[Byte]] {
    override def absorb[J](parser: AsyncParser[J], bytes: Array[Byte])(
        implicit rawFacade: RawFacade[J]): Either[ParseException, Seq[J]] = parser.absorb(bytes)
  }

  implicit def ByteSegmentAbsorbable[S <: Segment[Byte, _]]: Absorbable[S] = new Absorbable[S] {
    override def absorb[J](parser: AsyncParser[J], segment: S)(
      implicit rawFacade: RawFacade[J]): Either[ParseException, Seq[J]] = parser.absorb(segment.force.toArray)
  }

  implicit def ByteChunkAbsorbable[C <: Chunk[Byte]]: Absorbable[C] = new Absorbable[C] {
    override def absorb[J](parser: AsyncParser[J], chunk: C)(
        implicit rawFacade: RawFacade[J]): Either[ParseException, Seq[J]] = parser.absorb(chunk.toArray)
  }
}
