import jawn.{AsyncParser, Facade}
import scodec.bits.ByteVector
import scalaz.concurrent.Task
import scalaz.stream.{Channel, Process, io}

/**
 * Integrates the Jawn parser with scalaz-stream
 */
package object jawnstreamz {
  /**
   * A channel for parsing a source of ByteVectors into JSON values.
   *
   * This is the low-level interface.  See JsonSourceSyntax for more common usages.
   *
   * @param mode the Jawn async mode to parse in.
   * @param facade the Jawn facade for [[J]]
   * @tparam J the JSON AST to return.
   * @return a channel of Option[ByteVector] => Task[Seq[J]].  Send None to flush
   *         the parser and terminate.
   */
  def jsonR[J](mode: AsyncParser.Mode)(implicit facade: Facade[J]): Channel[Task, Option[ByteVector], Seq[J]] = {
    val acquire = Task.delay(AsyncParser[J](mode))
    def flush(parser: AsyncParser[J]) = Task.delay(parser.finish().fold(throw _, identity))
    def release(parser: AsyncParser[J]) = Task.delay(())
    def step(parser: AsyncParser[J]) = Task.now { bytes: ByteVector => Task.delay {
      parser.absorb(bytes.toByteBuffer).fold(throw _, identity)
    }}
    io.bufferedChannel(acquire)(flush _)(release _)(step _)
  }

  /**
   * Suffix syntax to asynchronously parse sources of ByteVectors to JSON.
   *
   * @param source the source process
   */
  implicit class JsonSourceSyntax(source: Process[Task, ByteVector]) {
    import Process._

    /**
     * Parses the source to a single JSON value.  If the stream is empty, parses to
     * the facade's concept of jnull.
     *
     * @param facade the Jawn facade for [[J]]
     * @tparam J the JSON AST to return
     * @return a task containing the parsed JSON result
     */
    def runJson[J](implicit facade: Facade[J]): Task[J] =
      throughJsonR(AsyncParser.SingleValue).runLastOr(facade.jnull())

    /**
     * Parses the source to a stream of JSON values.  Use if the source contains
     * multiple JSON elements concatenated together, or if the target is a stream.
     *
     * @param facade the Jawn facade for [[J]]
     * @tparam J the JSON AST to return
     * @return a process of the parsed JSON results
     */
    def jsonStream[J](implicit facade: Facade[J]): Process[Task, J] =
      throughJsonR(AsyncParser.ValueStream)

    /**
     * Parses the source as an array of JSON values.  Use to emit individual JSON
     *
     * entire array is parsed.
     *
     * @param facade the Jawn facade for [[J]]
     * @tparam J the JSON AST to return
     * @return a process of the JSON results wrapped in a JSON array
     */
    def unwrapJsonArray[J](implicit facade: Facade[J]): Process[Task, J] =
      throughJsonR(AsyncParser.UnwrapArray)

    private def throughJsonR[J](mode: AsyncParser.Mode)(implicit facade: Facade[J]): Process[Task, J] =
      source.throughOption(jsonR(mode)).flatMap(emitAll)
  }
}
