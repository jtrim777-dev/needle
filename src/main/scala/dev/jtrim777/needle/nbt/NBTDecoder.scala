package dev.jtrim777.needle.nbt

import net.minecraft.nbt.NbtElement

trait NBTDecoder[A] {
  def decode(source: NbtElement): A
}

object NBTDecoder {
  def apply[A](implicit instance: NBTDecoder[A]): NBTDecoder[A] = instance
}
