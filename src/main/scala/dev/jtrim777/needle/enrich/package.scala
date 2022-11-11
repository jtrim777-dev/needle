package dev.jtrim777.needle

import dev.jtrim777.needle.nbt.{NBTDecoder, NBTEncoder}
import dev.jtrim777.needle.nbt._
import net.minecraft.item.ItemStack

package object enrich {
  implicit class ItemStackEnrichment(is: ItemStack) {
    def load[A: NBTDecoder](key: String): A = is.getNbt.get(key).as[A]

    def put[A: NBTEncoder](key: String, value: A): ItemStack = {
      is.getNbt.put(key, value.asNBT)
      is
    }
  }
}
