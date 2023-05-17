package dev.jtrim777.needle.tile

import dev.jtrim777.needle.nbt.{NBTDecoder, NBTEncoder}
import net.minecraft.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.block.{Block, BlockState}
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.Packet
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.sound.{SoundCategory, SoundEvent}
import net.minecraft.util.math.BlockPos

abstract class BaseTile[X <: BaseTile[X, D], D : NBTEncoder : NBTDecoder](kind: BlockEntityType[_ <: X], pos: BlockPos,
                                          blockState: BlockState) extends BlockEntity(kind, pos, blockState) { this: X =>
  protected var data: D

  def serverTick(state: BlockState): Unit = {}
  def clientTick(state: BlockState, pos: BlockPos): Unit = {}

  override def toUpdatePacket: Packet[ClientPlayPacketListener] = {
    BlockEntityUpdateS2CPacket.create(this)
  }
  override def toInitialChunkDataNbt: NbtCompound = createNbt()
  private def syncClient(): Unit = {
    world.updateListeners(pos, blockState, blockState, Block.NOTIFY_LISTENERS)
  }

  override def readNbt(nbt: NbtCompound): Unit = {
    super.readNbt(nbt)

    Option(nbt.get("data")) match {
      case None => ()
      case Some(raw) =>
        this.data = implicitly[NBTDecoder[D]].decode(raw)
    }
  }
  override def writeNbt(nbt: NbtCompound): Unit = {
    super.writeNbt(nbt)

    nbt.put("data", implicitly[NBTEncoder[D]].encode(this.data))
  }

  private def cueSound(event: SoundEvent, category: SoundCategory = SoundCategory.BLOCKS): Unit = {
    this.world.playSound(null, this.pos, event, category, 1, 1)
  }
}
