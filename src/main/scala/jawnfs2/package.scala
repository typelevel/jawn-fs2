import fs2.util.{Catchable, Monad}
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
    * Emits individual JSON elements as they are parsed.
    *
    * @param facade the Jawn facade to materialize [[J]]
    * @tparam J the JSON AST to return
    */
  def parseJsonStream[F[_], A, J](implicit A: Absorbable[A], facade: Facade[J]): Pipe[F, A, J] =
    parseJson(AsyncParser.ValueStream)

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

    /**
      * Parses the source to a single JSON value.  If the stream is empty, parses to
      * the facade's concept of jnull.
      *
      * @param facade the Jawn facade to materialize [[J]]
      * @tparam J the JSON AST to return
      * @return the parsed JSON value, or the facade's concept of jnull if the source is empty
      */
    def runJson[J](implicit F: Monad[F], C: Catchable[F], absorbable: Absorbable[O], facade: Facade[J]): F[J] =
      stream.parseJson(AsyncParser.SingleValue).runFold(facade.jnull())((_, json) => json)

    /**
      * Emits individual JSON elements as they are parsed.
      *
      * @param facade the Jawn facade to materialize [[J]]
      * @tparam J the JSON AST to return
      */
    def parseJsonStream[J](implicit absorbable: Absorbable[O], facade: Facade[J]): Stream[F, J] =
      stream.through(jawnfs2.parseJsonStream)
  }
}
