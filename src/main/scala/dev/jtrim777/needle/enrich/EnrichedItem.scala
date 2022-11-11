package dev.jtrim777.needle.enrich

import dev.jtrim777.needle.nbt.{NBTDecoder, NBTEncoder}
import net.minecraft.item.{Item, ItemStack}

abstract class EnrichedItem[D : NBTEncoder : NBTDecoder](props: Item.Settings) extends Item(props) {
  protected val enrichmentKey: String = "data"

  protected def initialData(stack: ItemStack): D

  def load(stack: ItemStack): D = {
    if (stack.getNbt.contains(enrichmentKey)) {
      stack.load[D](enrichmentKey)
    } else {
      val dat = initialData(stack)
      stack.put(enrichmentKey, dat)
      dat
    }
  }

  def update(stack: ItemStack, data: D): Unit = {
    stack.put[D](enrichmentKey, data)
  }
}
