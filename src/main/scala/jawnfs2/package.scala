import fs2.{Chunk, Handle, Pipe, Pull, Stream}
import jawn.{AsyncParser, Facade}

import scala.language.higherKinds

/**
  * Integrates the Jawn parser with fs2
  */
package object jawnfs2 {

  /**
    * Parses to any Jawn-supported AST using the specified Async mode.
    *
    * @param facade the Jawn facade to materialize [[J]]
    * @tparam J the JSON AST to return
    * @param mode the async mode of the Jawn parser
    */
  def parseJson[F[_], A, J](mode: AsyncParser.Mode)(implicit A: Absorbable[A], facade: Facade[J]): Pipe[F, A, J] = {
    def go(parser: AsyncParser[J]): Handle[F, A] => Pull[F, J, Unit] =
      _.receive1Option {
        case Some((a, nextHandle)) =>
          val chunks = A.absorb(parser, a).fold(throw _, identity)
          Pull.output(Chunk.seq(chunks)) >> go(parser)(nextHandle)
        case None =>
          val remaining = parser.finish().fold(throw _, identity)
          Pull.output(Chunk.seq(remaining)) >> Pull.done
      }

    (src: Stream[F, A]) =>
      Stream.suspend {
        val parser = AsyncParser[J](mode)
        src.pull(go(parser))
      }
  }

  /**
    * Suffix syntax and convenience methods for parseJson
    */
  implicit class JsonStreamSyntax[F[_], O](stream: Stream[F, O]) {

    /**
      * Parses a source to any Jawn-supported AST using the specified Async mode.
      *
      * @param facade the Jawn facade to materialize [[J]]
      * @tparam J the JSON AST to return
      * @param mode the async mode of the Jawn parser
      */
    def parseJson[J](mode: AsyncParser.Mode)(implicit absorbable: Absorbable[O], facade: Facade[J]): Stream[F, J] =
      stream.through(jawnfs2.parseJson(mode))

  }
}
