package dev.jtrim777.needle.nbt

import net.minecraft.nbt.NbtElement

trait NBTEncoder[-A] {
  def encode(value: A): NbtElement
}
