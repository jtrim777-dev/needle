package dev.jtrim777.needle.enrich

import dev.jtrim777.needle.nbt.{NBTDecoder, NBTEncoder, NBTEOps, EncodableOps}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NbtCompound

case class EnrichedItemStack(underlying: ItemStack) {
  def count: Int = underlying.getCount
  def item: Item = underlying.getItem
  def isEmpty: Boolean = underlying.isEmpty

  def tag: NbtCompound = underlying.getNbt

  def load[A : NBTDecoder](key: String): A = tag.get(key).as[A]

  def put[A : NBTEncoder](key: String, value: A): EnrichedItemStack = {
    tag.put(key, value.asNBT)
    this
  }
}
