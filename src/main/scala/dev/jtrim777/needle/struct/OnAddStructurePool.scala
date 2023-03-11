package dev.jtrim777.needle.struct

import net.fabricmc.fabric.api.event.{Event, EventFactory}
import net.minecraft.structure.pool.StructurePool

trait OnAddStructurePool {
  def invoke(pool: StructurePool): Unit
}

object OnAddStructurePool {
  val Event: Event[OnAddStructurePool] = EventFactory.createArrayBacked(
    classOf[OnAddStructurePool],
    { (listeners: Array[OnAddStructurePool]) =>
      ((pool: StructurePool) => listeners.foreach(_.invoke(pool))) : OnAddStructurePool
    }
  )
}
