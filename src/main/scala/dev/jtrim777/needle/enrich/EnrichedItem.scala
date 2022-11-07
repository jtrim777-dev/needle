package dev.jtrim777.needle.enrich

import dev.jtrim777.needle.nbt.{NBTDecoder, NBTEncoder}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NbtElement
import scala.collection.mutable.{ListBuffer => MutSeq}

trait EnrichedItem { this: Item =>

  private val baseEnrichments: MutSeq[(String, EnrichedItemStack => NbtElement)] = MutSeq.empty

  protected def enrichNew(stack: EnrichedItemStack): EnrichedItemStack = {
    baseEnrichments.foreach { case (name, prov) => stack.tag.put(name, prov(stack)) }
    stack
  }

  protected def registerEnrichment[A : NBTEncoder](key: String, producer: EnrichedItemStack => A): Unit = {
    val prov = {eis:EnrichedItemStack => implicitly[NBTEncoder[A]].encode(producer(eis))}
    baseEnrichments.append((key, prov))
  }

  def apply(stack: ItemStack): EnrichedItemStack = {
    val base = EnrichedItemStack(stack)

    if (base.tag.contains("__needle_enrich")) {
      base
    } else {
      base.put("__needle_enrich", true)
      enrichNew(base)
    }
  }
}
