package dev.jtrim777.needle.nbt

import net.minecraft.nbt.NbtElement

trait NBTDecoder[A] {
  def decode(source: NbtElement): A
}
