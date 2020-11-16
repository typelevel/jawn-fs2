package org.typelevel.jawn.fs2

private[this] class SeqWrapper[A](underlying: collection.Seq[A])
    extends collection.immutable.Seq[A] {
  def iterator: Iterator[A] = underlying.iterator
  def apply(i: Int): A = underlying(i)
  def length: Int = underlying.length
}
