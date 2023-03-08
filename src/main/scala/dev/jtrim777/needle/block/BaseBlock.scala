package dev.jtrim777.needle.block

import net.minecraft.block.{AbstractBlock, Block, BlockState}
import net.minecraft.util.math.BlockPos

import scala.reflect.ClassTag

class BaseBlock[X <: BaseBlock[X] : ClassTag](props: AbstractBlock.Settings) extends Block(props) { this: X =>
  type Inst = BBlock[X]

  def inst(state: BlockState, pos: BlockPos): Inst =
    BBlock[X](state, pos)
}
