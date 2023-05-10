package dev.jtrim777.needle.nbt

import net.minecraft.nbt._

object NBTPredicate {
  private def matchList(pattern: NbtList, tgt: NbtElement): Boolean = {
    tgt.asOpt[Seq[NbtElement]].exists { seq =>
      val patterns = pattern.as[Seq[NbtElement]]
        .groupBy(a => a)
        .map(p => (p._1, p._2.length))

      patterns.forall { case (elem, count) => seq.count(e => e == elem) >= count }
    }
  }

  private def matchObject(pattern: NbtCompound, tgt: NbtElement): Boolean = {
    tgt.asOpt[Map[String, NbtElement]].exists { obj =>
      val po = pattern.as[Map[String, NbtElement]]

      po.forall { case (key, vpat) =>
        obj.get(key).exists( v => matches(vpat, v) )
      }
    }
  }

  def matches(pattern: NbtElement, tgt: NbtElement): Boolean = {
    if (pattern.getType == NbtElement.LIST_TYPE) {
      matchList(pattern.asInstanceOf[NbtList], tgt)
    } else if (pattern.getType == NbtElement.COMPOUND_TYPE) {
      matchObject(pattern.asInstanceOf[NbtCompound], tgt)
    } else {
      pattern == tgt
    }
  }
}
