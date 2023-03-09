package dev.jtrim777.needle.nbt

import net.minecraft.nbt.NbtElement

trait NBTEncoder[-A] {
  def encode(value: A): NbtElement

  def contramap[B](f: B => A): NBTEncoder[B] = { b => this.encode(f(b)) }
}

object NBTEncoder {
  def apply[A](implicit instance: NBTEncoder[A]): NBTEncoder[A] = instance
}

