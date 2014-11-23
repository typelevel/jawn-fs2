import jawn.{AsyncParser, Facade}

import scala.language.higherKinds
import scalaz.{Monad, Catchable}
import scalaz.stream._

/**
 * Integrates the Jawn parser with scalaz-stream
 */
package object jawnstreamz {
  /**
   * Parses to any Jawn-supported AST using the specified Async mode.
   *
   * @param facade the Jawn facade to materialize [[J]]
   * @tparam J the JSON AST to return
   * @param mode the async mode of the Jawn parser
   */
  def parseJson[A, J](mode: AsyncParser.Mode)(implicit A: Absorbable[A], facade: Facade[J]): Process1[A, J] = {
    import Process._
    import process1._

    def go(parser: AsyncParser[J]): Process1[A, J] =
      await1[A].flatMap { a =>
        val chunks = A.absorb(parser, a).fold(throw _, identity)
        emitAll(chunks) fby go(parser)
      }

    suspend {
      val parser = AsyncParser[J](mode)
      go(parser).onComplete {
        emitAll(parser.finish().fold(throw _, identity))
      }
    }
  }

  /**
   * Emits individual JSON elements as they are parsed.
   *
   * @param facade the Jawn facade to materialize [[J]]
   * @tparam J the JSON AST to return
   */
  def parseJsonStream[A, J](implicit A: Absorbable[A], facade: Facade[J]): Process1[A, J] =
    parseJson(AsyncParser.ValueStream)

  /**
   * Emits elements of an outer JSON array as they are parsed.
   *
   * @param facade the Jawn facade to materialize [[J]]
   * @tparam J the JSON AST to return
   */
  def unwrapJsonArray[A, J](implicit A: Absorbable[A], facade: Facade[J]): Process1[A, J] =
    parseJson(AsyncParser.UnwrapArray)

  /**
   * Suffix syntax and convenience methods for parseJson.
   */
  implicit class JsonSourceSyntax[F[_], O](source: Process[F, O]) {
    import Process._

    /**
     * Parses a source to any Jawn-supported AST using the specified Async mode.
     *
     * @param facade the Jawn facade to materialize [[J]]
     * @tparam J the JSON AST to return
     * @param mode the async mode of the Jawn parser
     */
    def parseJson[J](mode: AsyncParser.Mode)(implicit absorbable: Absorbable[O], facade: Facade[J]): Process[F, J] =
      source.pipe(jawnstreamz.parseJson(mode))

    /**
     * Parses the source to a single JSON value.  If the stream is empty, parses to
     * the facade's concept of jnull.
     *
     * @param facade the Jawn facade to materialize [[J]]
     * @tparam J the JSON AST to return
     * @return the parsed JSON value, or the facade's concept of jnull if the source is empty
     */
    def runJson[J](implicit F: Monad[F], C: Catchable[F], absorbable: Absorbable[O], facade: Facade[J]): F[J] =
      source.parseJson(AsyncParser.SingleValue).runLastOr(facade.jnull())

    /**
     * Emits individual JSON elements as they are parsed.
     *
     * @param facade the Jawn facade to materialize [[J]]
     * @tparam J the JSON AST to return
     */
    def parseJsonStream[J](implicit absorbable: Absorbable[O], facade: Facade[J]): Process[F, J] =
      source.pipe(jawnstreamz.parseJsonStream)

    /**
     * Emits elements of an outer JSON array as they are parsed.
     *
     * @param facade the Jawn facade to materialize [[J]]
     * @tparam J the JSON AST to return
     */
    def unwrapJsonArray[J](implicit absorbable: Absorbable[O], facade: Facade[J]): Process[F, J] =
      source.pipe(jawnstreamz.unwrapJsonArray)
  }
}
