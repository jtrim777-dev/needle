package dev.jtrim777.needle.util

import scala.collection.mutable.{Map => MMap}

class BiMultiMap[K, V] {
  protected val forward: MMap[K, List[V]] = MMap.empty
  protected val backward: MMap[V, K] = MMap.empty

  def add(k: K, v: V): Unit = {
    forward(k) = forward.getOrElse(k, List.empty).appended(v)
    backward(v) = k
  }

  def get(k: K): Option[List[V]] = forward.get(k)
  def revGet(v: V): Option[K] = backward.get(v)

  def apply(k: K): List[V] = forward(k)
  def rev(v: V): K = backward(v)

  def contains(k: K): Boolean = forward.contains(k)
  def produces(v: V): Boolean = backward.contains(v)
}

object BiMultiMap {
  def fromSeq[K, V](seq: Seq[(K, V)]): BiMultiMap[K, V] = {
    val base = new BiMultiMap[K, V]

    seq.foreach { case (k,v) => base.add(k, v) }

    base
  }

  def fromMap[K, V](map: Map[K, V]): BiMultiMap[K, V] = BiMultiMap.fromSeq(map.toSeq)

  def empty[K, V]: BiMultiMap[K, V] = new BiMultiMap[K, V]

  def apply[K, V](items: (K, V)*): BiMultiMap[K, V] = fromSeq(items)
}
