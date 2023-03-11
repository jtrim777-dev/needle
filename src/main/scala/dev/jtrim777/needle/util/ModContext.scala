package dev.jtrim777.needle.util

trait ModContext {
  val modID: String

  def namespace: String = modID
}

object ModContext {
  case class Simple(id: String) extends ModContext {
    override val modID: String = id
  }
}
