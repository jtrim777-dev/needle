package dev.jtrim777.needle

import dev.jtrim777.needle.nbt.{NBTDecoder, NBTEncoder}
import dev.jtrim777.needle.nbt._
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound

package object enrich {
  implicit class ItemStackEnrichment(is: ItemStack) {
    def tag: NbtCompound = is.getOrCreateNbt()

    def load[A: NBTDecoder](key: String): A = is.tag.get(key).as[A]

    def put[A: NBTEncoder](key: String, value: A): ItemStack = {
      is.tag.put(key, value.asNBT)
      is
    }
  }
}
