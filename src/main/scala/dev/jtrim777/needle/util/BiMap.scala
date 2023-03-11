package dev.jtrim777.needle.util

import scala.collection.mutable.{Map => MMap}

class BiMap[K, V] {
  protected val forward: MMap[K, V] = MMap.empty
  protected val backward: MMap[V, K] = MMap.empty

  def add(k: K, v: V): Unit = {
    forward(k) = v
    backward(v) = k
  }

  def get(k: K): Option[V] = forward.get(k)
  def revGet(v: V): Option[K] = backward.get(v)

  def apply(k: K): V = forward(k)
  def rev(v: V): K = backward(v)

  def contains(k: K): Boolean = forward.contains(k)
  def produces(v: V): Boolean = backward.contains(v)
}

object BiMap {
  def fromSeq[K, V](seq: Seq[(K, V)]): BiMap[K, V] = {
    val base = new BiMap[K, V]

    seq.foreach { case (k,v) => base.add(k, v) }

    base
  }

  def fromMap[K, V](map: Map[K, V]): BiMap[K, V] = BiMap.fromSeq(map.toSeq)

  def empty[K, V]: BiMap[K, V] = new BiMap[K, V]

  def apply[K, V](items: (K, V)*): BiMap[K, V] = fromSeq(items)
}
