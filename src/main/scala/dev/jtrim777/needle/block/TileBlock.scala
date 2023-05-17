package dev.jtrim777.needle.block

import dev.jtrim777.needle.tile.BaseTile
import net.minecraft.block.AbstractBlock.Settings
import net.minecraft.block.{BlockEntityProvider, BlockState}
import net.minecraft.block.entity.{BlockEntity, BlockEntityTicker, BlockEntityType}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

import scala.reflect.ClassTag

abstract class TileBlock[X <: TileBlock[X, T] : ClassTag, T <: BaseTile[T]](props: Settings)
  extends BaseBlock[X](props) with BlockEntityProvider { this: X =>

  def getTileType: BlockEntityType[T]

  def doesTickServer: Boolean
  def doesTickClient: Boolean

  override def onSyncedBlockEvent(state: BlockState, world: World,
                                  pos: BlockPos, typeId: Int, data: Int): Boolean = {
    super.onSyncedBlockEvent(state, world, pos, typeId, data)

    world.getBlockEntity(pos) match {
      case null => false
      case be => be.onSyncedBlockEvent(typeId, data)
    }
  }

  override def getTicker[E <: BlockEntity](world: World, state: BlockState,
                                           etype: BlockEntityType[E]): BlockEntityTicker[E] = {
    val valid = getTileType

    etype match {
      case `valid` => if (world.isClient && doesTickClient) {
        { (_, p, s, e) => e.asInstanceOf[T].clientTick(s, p) }
      } else if (!world.isClient && doesTickServer) {
        { (_, _, s, e) => e.asInstanceOf[T].serverTick(s) }
      } else null
      case _ => null
    }
  }


}
