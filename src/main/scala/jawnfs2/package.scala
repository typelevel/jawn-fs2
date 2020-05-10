import cats.ApplicativeError
import cats.effect.Sync
import fs2.{Chunk, Pipe, Pull, Stream}
import org.typelevel.jawn.{AsyncParser, Facade, ParseException}

/**
  * Integrates the Jawn parser with fs2
  */
package object jawnfs2 {

  /**
    * Parses to any Jawn-supported AST using the specified Async mode.
    *
    * @param facade the Jawn facade to materialize `J`
    * @tparam J the JSON AST to return
    * @param mode the async mode of the Jawn parser
    */
  def parseJson[F[_], A, J](mode: AsyncParser.Mode)(
      implicit F: ApplicativeError[F, Throwable],
      A: Absorbable[A],
      facade: Facade[J]): Pipe[F, A, J] = {
    def go(parser: AsyncParser[J])(s: Stream[F, A]): Pull[F, J, Unit] = {
      def handle(attempt: Either[ParseException, collection.Seq[J]]) =
        attempt.fold(Pull.raiseError[F], js => Pull.output(Chunk.seq(js)))

      s.pull.uncons1.flatMap {
        case Some((a, stream)) =>
          handle(A.absorb(parser, a)) >> go(parser)(stream)
        case None =>
          handle(parser.finish()) >> Pull.done
      }
    }

    src => go(AsyncParser[J](mode))(src).stream
  }

  /**
    * Emits individual JSON elements as they are parsed.
    *
    * @param facade the Jawn facade to materialize `J`
    * @tparam J the JSON AST to return
    */
  def parseJsonStream[F[_], A, J](
      implicit F: ApplicativeError[F, Throwable],
      A: Absorbable[A],
      facade: Facade[J]): Pipe[F, A, J] =
    parseJson(AsyncParser.ValueStream)

  /**
    * Emits elements of an outer JSON array as they are parsed.
    *
    * @param facade the Jawn facade to materialize `J`
    * @tparam J the JSON AST to return
    */
  def unwrapJsonArray[F[_], A, J](
      implicit F: ApplicativeError[F, Throwable],
      A: Absorbable[A],
      facade: Facade[J]): Pipe[F, A, J] =
    parseJson(AsyncParser.UnwrapArray)

  /**
    * Suffix syntax and convenience methods for parseJson
    */
  implicit class JsonStreamSyntax[F[_], O](stream: Stream[F, O]) {

    /**
      * Parses a source to any Jawn-supported AST using the specified Async mode.
      *
      * @param facade the Jawn facade to materialize `J`
      * @tparam J the JSON AST to return
      * @param mode the async mode of the Jawn parser
      */
    def parseJson[J](mode: AsyncParser.Mode)(
        implicit F: ApplicativeError[F, Throwable],
        A: Absorbable[O],
        facade: Facade[J]): Stream[F, J] =
      stream.through(jawnfs2.parseJson(mode))

    /**
      * Parses the source to a single JSON optional JSON value.
      *
      * @param facade the Jawn facade to materialize `J`
      * @tparam J the JSON AST to return
      * @return some parsed JSON value, or None if the source is empty
      */
    def runJsonOption[J](implicit F: Sync[F], A: Absorbable[O], facade: Facade[J]): F[Option[J]] =
      stream.parseJson(AsyncParser.SingleValue).compile.last

    /**
      * Emits individual JSON elements as they are parsed.
      *
      * @param facade the Jawn facade to materialize `J`
      * @tparam J the JSON AST to return
      */
    def parseJsonStream[J](
        implicit F: ApplicativeError[F, Throwable],
        A: Absorbable[O],
        facade: Facade[J]): Stream[F, J] =
      stream.through(jawnfs2.parseJsonStream)

    /**
      * Emits elements of an outer JSON array as they are parsed.
      *
      * @param facade the Jawn facade to materialize `J`
      * @tparam J the JSON AST to return
      */
    def unwrapJsonArray[J](
        implicit F: ApplicativeError[F, Throwable],
        A: Absorbable[O],
        facade: Facade[J]): Stream[F, J] =
      stream.through(jawnfs2.unwrapJsonArray)
  }
}
