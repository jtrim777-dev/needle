package dev.jtrim777.needle.registry

import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.{LogManager, Logger}

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

abstract class DelayedRegistry[T : ClassTag] {
  def registry: Registry[T]
  def namespace: String

  protected val entryClass: Class[_] = implicitly[ClassTag[T]].runtimeClass
  protected def entryClassName: String = entryClass.getSimpleName.stripSuffix("$").toLowerCase
  protected val log: Logger =
    LogManager.getLogger(s"$namespace:$entryClassName-registry")

  private var entryLock: Boolean = false
  private val entries: scala.collection.mutable.Map[String, () => T] = scala.collection.mutable.Map()

  def addEntry(id: String, value: => T): Unit = if (entryLock) {
    throw new IllegalStateException(s"Cannot add entry to delayed registry ${this.getClass.getSimpleName} after registration")
  } else {
    this.log.info(s"Added entry '$id'")
    this.entries.put(id, () => value)
  }

  def register(): Unit = {
    val rx = registry
    val ns = namespace
    var ct = 0
    entries.foreach { t =>
      Registry.register(rx, new Identifier(ns, t._1), t._2())
      ct += 1
    }
    this.log.info(s"Dumped $ct entries to runtime registry")
    this.entryLock = true
  }

  def lookup(id: String): T = {
    val ident = new Identifier(namespace, id)

    if (registry.containsId(ident)) {
      registry.get(ident)
    } else if (entries.contains(id)) {
      Try(entries(id)()) match {
        case Failure(exception) =>
          log.warn(s"Tried to access $entryClassName '$namespace:$id' before it could be initialized; Initialization failed w/ $exception")
          throw new NoSuchElementException(s"No such $entryClassName '$namespace:$id' in registry ${registry.getKey.toString} (entry has not yet been registered)")
        case Success(value) => value
      }
    } else {
      throw new NoSuchElementException(s"No such $entryClassName '$namespace:$id' in registry ${registry.getKey.toString}")
    }
  }

  def entry(id: String, value: T): (Identifier, T) = (new Identifier(namespace, id), value)
}
