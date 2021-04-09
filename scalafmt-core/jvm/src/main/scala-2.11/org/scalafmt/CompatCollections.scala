package org.scalafmt

import scala.collection.mutable
import scala.collection.concurrent.TrieMap

object CompatCollections {
  val JavaConverters = scala.collection.JavaConverters
  val ParConverters = scala.collection.parallel.CollectionConverters
  def concurrentMap[K,V]: mutable.Map[K, V] = TrieMap.empty[K, V]
  def concurrentQueue[K]: LinkedBlockingDeque[K] =  new LinkedBlockingDeque[K]
}
