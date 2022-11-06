package dev.jtrim777.needle.registry

import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

trait DelayedRegistry[T] {
  def registry: Registry[T]

  private var entryLock: Boolean = false
  private val entries: scala.collection.mutable.Map[String, () => T] = scala.collection.mutable.Map()

  def addEntry(id: String, value: => T): Unit = if (entryLock) {
    throw new IllegalStateException(s"Cannot add entry to delayed registry ${this.getClass.getSimpleName} after registration")
  } else {
    this.entries.put(id, () => value)
  }

  def namespace: String

  def register(): Unit = {
    val rx = registry
    val ns = namespace
    entries.foreach(t => Registry.register(rx, new Identifier(ns, t._1), t._2()))
    this.entryLock = true
  }

  def entry(id: String, value: T): (Identifier, T) = (new Identifier(namespace, id), value)
}
