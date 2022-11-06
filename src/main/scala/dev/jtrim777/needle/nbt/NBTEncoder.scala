package dev.jtrim777.needle.nbt

import net.minecraft.nbt.NbtElement

trait NBTEncoder[-A] {
  def encode(value: A): NbtElement
}

object NBTEncoder {
  def apply[A](implicit instance: NBTEncoder[A]): NBTEncoder[A] = instance
}

