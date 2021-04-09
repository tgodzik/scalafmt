package org.scalafmt

import scala.collection.mutable
import java.util.concurrent.TimeUnit

object CompatCollections {
  val JavaConverters = scala.jdk.CollectionConverters

  // Scala native doesn't support concurrency
  object ParConverters {
    implicit class XIterable[T](col: Iterable[T]) {
      def par = col
    }
  }
  def concurrentMap[K,V]: mutable.Map[K, V] = mutable.Map.empty[K,V]

  class ConcurrentQueue[K] extends mutable.Queue[K] {
    def put(k : K) = super.enqueue(k)
    def poll(t: Long, unit : TimeUnit) = super.dequeue()
  }
  def concurrentQueue[K] = new ConcurrentQueue[K]
}
