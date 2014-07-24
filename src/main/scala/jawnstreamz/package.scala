import jawn.{ParseException, AsyncParser, Facade}
import scodec.bits.ByteVector
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
  def parseJson[J](mode: AsyncParser.Mode)(implicit facade: Facade[J]): Process1[ByteVector, J] = {
    import Process._
    process1.suspend1 {
      val parser = AsyncParser[J](mode)
      def withParser(f: AsyncParser[J] => Either[ParseException, Seq[J]]) = emitSeq(f(parser).fold(throw _, identity))
      receive1({bytes: ByteVector => withParser(_.absorb(bytes.toByteBuffer))}, withParser(_.finish())).repeat
    }
  }

  /**
   * Emits individual JSON elements as they are parsed.
   *
   * @param facade the Jawn facade to materialize [[J]]
   * @tparam J the JSON AST to return
   */
  def parseJsonStream[J](implicit facade: Facade[J]): Process1[ByteVector, J] = parseJson(AsyncParser.ValueStream)

  /**
   * Emits elements of an outer JSON array as they are parsed.
   *
   * @param facade the Jawn facade to materialize [[J]]
   * @tparam J the JSON AST to return
   */
  def unwrapJsonArray[J](implicit facade: Facade[J]): Process1[ByteVector, J] = parseJson(AsyncParser.UnwrapArray)

  /**
   * Suffix syntax and convenience methods for parseJson.
   */
  implicit class JsonSourceSyntax[F[_]](source: Process[F, ByteVector]) {
    import Process._

    /**
     * Parses a source of ByteVectors to any Jawn-supported AST using the specified Async mode.
     *
     * @param facade the Jawn facade to materialize [[J]]
     * @tparam J the JSON AST to return
     * @param mode the async mode of the Jawn parser
     */
    def parseJson[J](mode: AsyncParser.Mode)(implicit facade: Facade[J]): Process[F, J] =
      source.pipe(jawnstreamz.parseJson(mode))

    /**
     * Parses the source to a single JSON value.  If the stream is empty, parses to
     * the facade's concept of jnull.
     *
     * @param facade the Jawn facade to materialize [[J]]
     * @tparam J the JSON AST to return
     * @return the parsed JSON value, or the facade's concept of jnull if the source is empty
     */
    def runJson[J](implicit F: Monad[F], C: Catchable[F], facade: Facade[J]): F[J] =
      source.parseJson(AsyncParser.SingleValue).runLastOr(facade.jnull())

    /**
     * Emits individual JSON elements as they are parsed.
     *
     * @param facade the Jawn facade to materialize [[J]]
     * @tparam J the JSON AST to return
     */
    def parseJsonStream[J](implicit facade: Facade[J]): Process[F, J] = source.pipe(jawnstreamz.parseJsonStream)

    /**
     * Emits elements of an outer JSON array as they are parsed.
     *
     * @param facade the Jawn facade to materialize [[J]]
     * @tparam J the JSON AST to return
     */
    def unwrapJsonArray[J](implicit facade: Facade[J]): Process[F, J] = source.pipe(jawnstreamz.unwrapJsonArray)
  }
}
