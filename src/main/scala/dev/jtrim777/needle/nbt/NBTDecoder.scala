package dev.jtrim777.needle.nbt

import net.minecraft.nbt.NbtElement

trait NBTDecoder[A] {
  def decode(source: NbtElement): A

  def map[B](f: A => B): NBTDecoder[B] = { e => f(this.decode(e)) }
}

object NBTDecoder {
  def apply[A](implicit instance: NBTDecoder[A]): NBTDecoder[A] = instance
}
