package dev.jtrim777.needle.struct

import scala.collection.mutable.{Map => MMap}
import net.minecraft.structure.pool.StructurePool.Projection
import net.minecraft.structure.pool.{ListPoolElement, StructurePool, StructurePoolElement, StructurePoolElementType}
import net.minecraft.structure.processor.{StructureProcessorList, StructureProcessorLists}
import net.minecraft.util.Identifier
import net.minecraft.util.registry.RegistryEntry
import net.minecraft.world.gen.feature.PlacedFeature

object StructurePoolRegistry {
  sealed trait Entry {
    val id: Identifier
    val weight: Int
    val processor: RegistryEntry[StructureProcessorList]
    val projection: Projection
    val elemType: StructurePoolElementType[_]

    def getElement: StructurePoolElement
  }

  case class SimpleEntry(id: Identifier, weight: Int,
                         processor: RegistryEntry[StructureProcessorList] = StructureProcessorLists.EMPTY,
                         projection: Projection = Projection.RIGID) extends Entry {
    override val elemType: StructurePoolElementType[_] = StructurePoolElementType.LEGACY_SINGLE_POOL_ELEMENT

    override def getElement: StructurePoolElement = {
      StructurePoolElement.ofProcessedLegacySingle(
        id.toString,
        processor
      ).apply(projection)
    }
  }

  case class SingleEntry(id: Identifier, weight: Int, processor: RegistryEntry[StructureProcessorList],
                         projection: Projection) extends Entry {
    override val elemType: StructurePoolElementType[_] = StructurePoolElementType.SINGLE_POOL_ELEMENT

    override def getElement: StructurePoolElement = StructurePoolElement.ofProcessedSingle(
      id.toString,
      processor
    ).apply(projection)
  }

  case class ListEntry(element: ListPoolElement, weight: Int) extends Entry {
    override val id: Identifier = Identifier.of("minecraft", "air")
    override val elemType: StructurePoolElementType[_] = StructurePoolElementType.LIST_POOL_ELEMENT
    override val processor: RegistryEntry[StructureProcessorList] = StructureProcessorLists.EMPTY
    override val projection: Projection = Projection.RIGID

    override def getElement: StructurePoolElement = element
  }

  case class FeatureEntry(weight: Int, projection: Projection,
                          feature: RegistryEntry[PlacedFeature]) extends Entry {
    override val id: Identifier = Identifier.of("minecraft", "air")
    override val elemType: StructurePoolElementType[_] = StructurePoolElementType.LEGACY_SINGLE_POOL_ELEMENT
    override val processor: RegistryEntry[StructureProcessorList] = StructureProcessorLists.EMPTY

    override def getElement: StructurePoolElement = StructurePoolElement.ofFeature(feature).apply(projection)
  }

  protected val pools: MMap[Identifier, List[Entry]] = MMap.empty

  def register(pool: Identifier)(entry: Entry): Unit = {
    pools.put(pool, pools.getOrElse(pool, List.empty).appended(entry))
  }

  def registerSimple(pool: Identifier)(id: Identifier, weight: Int = 1,
                                       projection: Projection = Projection.RIGID,
                                       processor: RegistryEntry[StructureProcessorList] = StructureProcessorLists.EMPTY): Unit = {
    register(pool)(SimpleEntry(id, weight, processor, projection))
  }

  def registerList(pool: Identifier)(element: ListPoolElement, weight: Int = 1): Unit = {
    register(pool)(ListEntry(element, weight))
  }

  def registerFeature(pool: Identifier)(feature: RegistryEntry[PlacedFeature], weight: Int = 1,
                                        projection: Projection = Projection.RIGID): Unit = {
    register(pool)(FeatureEntry(weight, projection, feature))
  }

  def dumpToRuntimePool(pool: StructurePool): Unit = {
    for (entry <- pools.getOrElse(pool.getId, List.empty)) {
      pool.addElement(entry.getElement, entry.weight)
    }
  }

  OnAddStructurePool.Event.register(StructurePoolRegistry.dumpToRuntimePool)
}
