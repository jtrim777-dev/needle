package dev.jtrim777.needle.block

import net.minecraft.block.{Block, BlockState}
import net.minecraft.state.property.Property
import net.minecraft.util.math.{BlockPos, Direction, Vec3i}
import net.minecraft.world.{BlockView, ModifiableWorld}

import scala.reflect.ClassTag

case class BBlock[+B <: Block : ClassTag](state: BlockState, pos: BlockPos) {
  private def xclass: Class[_] = implicitly[ClassTag[B]].runtimeClass

  assert(xclass.isAssignableFrom(state.getBlock.getClass))

  def block: B = state.getBlock.asInstanceOf[B]

  def stateWith[T <: Comparable[T], V <: T](prop: Property[T], value: V): BlockState =
    this.state.`with`(prop, value)

  def get[T <: Comparable[T]](prop: Property[T]): T = this.state.get(prop)

  def overwrite(nstate: BlockState, world: ModifiableWorld): Unit = world.setBlockState(pos, nstate, 2)

  def neighbor(direction: Direction, offset: Int, world: BlockView): BBlock[Block] = {
    val np = pos.asInstanceOf[Vec3i].offset(direction, offset).asInstanceOf[BlockPos]
    val ns = world.getBlockState(np)
    BBlock[Block](ns, np)
  }

  def sibling(direction: Direction, offset: Int, world: BlockView): Option[BBlock[B]] = {
    val base = neighbor(direction, offset, world)

    if (xclass.isAssignableFrom(base.state.getBlock.getClass)) {
      Some(BBlock[B](base.state, base.pos))
    } else None
  }

  def up(world: BlockView): BBlock[Block] = neighbor(Direction.UP, 1, world)
  def down(world: BlockView): BBlock[Block] = neighbor(Direction.DOWN, 1, world)
  def west(world: BlockView): BBlock[Block] = neighbor(Direction.WEST, 1, world)
  def east(world: BlockView): BBlock[Block] = neighbor(Direction.EAST, 1, world)
  def north(world: BlockView): BBlock[Block] = neighbor(Direction.NORTH, 1, world)
  def south(world: BlockView): BBlock[Block] = neighbor(Direction.SOUTH, 1, world)

  def selfUp(world: BlockView): Option[BBlock[B]] = sibling(Direction.UP, 1, world)
  def selfDown(world: BlockView): Option[BBlock[B]] = sibling(Direction.DOWN, 1, world)
  def selfWest(world: BlockView): Option[BBlock[B]] = sibling(Direction.WEST, 1, world)
  def selfEast(world: BlockView): Option[BBlock[B]] = sibling(Direction.EAST, 1, world)
  def selfNorth(world: BlockView): Option[BBlock[B]] = sibling(Direction.NORTH, 1, world)
  def selfSouth(world: BlockView): Option[BBlock[B]] = sibling(Direction.SOUTH, 1, world)
}
