package dev.jtrim777.needle.enrich

import dev.jtrim777.needle.nbt.{NBTDecoder, NBTEncoder}
import net.minecraft.item.{Item, ItemStack}

import scala.reflect.ClassTag

abstract class EnrichedItem[D : NBTEncoder : NBTDecoder](props: Item.Settings)(implicit val ct: ClassTag[D]) extends Item(props) {
  protected val enrichmentKey: String = "data"

  protected def initialData(stack: ItemStack): D

  def load(stack: ItemStack): D = {
    if (stack.getItem.toString != this.toString) {
      throw new IllegalArgumentException(s"Cannot load data for item ${this.toString} from item stack containing ${stack.getItem.toString}")
    }

    if (stack.tag.contains(enrichmentKey)) {
      stack.load[D](enrichmentKey)
    } else {
      val dat = initialData(stack)
      stack.put(enrichmentKey, dat)
      dat
    }
  }

  def update(stack: ItemStack, data: D): Unit = {
    if (stack.getItem.toString != this.toString) {
      throw new IllegalArgumentException(s"Cannot save data for item ${this.toString} onto item stack containing ${stack.getItem.toString}")
    }

    stack.put[D](enrichmentKey, data)
  }
}

object EnrichedItem {
  def attemptLoad[A : NBTDecoder](is: ItemStack)(implicit ct: ClassTag[A]): Option[A] = {
    is.getItem match {
      case ei: EnrichedItem[A] if ei.ct == ct => Some(ei.load(is))
      case _ => None
    }
  }

  def load[A: NBTDecoder](is: ItemStack)(implicit ct: ClassTag[A]): A = {
    is.getItem match {
      case ei: EnrichedItem[A] if ei.ct == ct => ei.load(is)
      case _ => throw new IllegalArgumentException(s"$is does not contain an EnrichedItem[${ct.runtimeClass.getSimpleName}]")
    }
  }
}
