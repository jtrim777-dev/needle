package dev.jtrim777.needle

import com.mojang.datafixers.util.Pair
import dev.jtrim777.needle.mixin.StructurePoolAccessor
import net.minecraft.structure.pool.{StructurePool, StructurePoolElement}

import java.util.{ArrayList => JAList, List => JList}

package object struct {
  implicit class PoolOps(pool: StructurePool) {
    def access: StructurePoolAccessor = pool.asInstanceOf[StructurePoolAccessor]

    def elementCounts: JList[Pair[StructurePoolElement, Integer]] = access.getElementCounts
    def elementCounts_=(nval: JList[Pair[StructurePoolElement, Integer]]): Unit = access.setElementCounts(nval)

    def elements: JList[StructurePoolElement] = access.getElements

    def addElement(element: StructurePoolElement, weight: Int = 1): Unit = {
      if (elementCounts.isInstanceOf[JAList[_]]) {
        elementCounts.add(Pair.of(element, weight))
      } else {
        val nelemCts = new JAList[Pair[StructurePoolElement, Integer]](elementCounts)
        nelemCts.add(Pair.of(element, weight))
        elementCounts = nelemCts
      }

      (0 until weight).foreach { _ => elements.add(element) }
    }
  }
}
