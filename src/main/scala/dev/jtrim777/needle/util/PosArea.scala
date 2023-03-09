package dev.jtrim777.needle.util

import net.minecraft.util.math.{BlockPos, Vec3i}
import syntax._

sealed abstract class PosArea(val root: BlockPos) extends Iterable[BlockPos] {

}

object PosArea {
  case class Cubic(center: BlockPos, dx: Int, dy: Int, dz: Int) extends PosArea(center) {
    case class Pointer(focus: BlockPos, rx: Int, ry: Int, rz: Int)

    class Itty(var ptr: Pointer) extends Iterator[BlockPos] {
      override def hasNext: Boolean = ptr.rx > 0 || ptr.ry > 0 || ptr.rz > 0

      override def next(): BlockPos = {
        if (ptr.rx > 0) {
          val next = ptr.focus.offset(1, 0, 0)
          this.ptr = this.ptr.copy(focus = next, rx = ptr.rx - 1)
          next
        } else if (ptr.rz > 0) {
          val next = ptr.focus.offset(-dz, 0, 1)
          this.ptr = this.ptr.copy(focus = next, rz = ptr.rz - 1)
          next
        } else if (ptr.ry > 0) {
          val next = ptr.focus.offset(-dx, 1, -dz)
          this.ptr = this.ptr.copy(focus = next, ry = ptr.ry - 1)
          next
        } else {
          throw new NoSuchElementException("BlockPos iterator is empty")
        }
      }
    }

    override def iterator: Iterator[BlockPos] =
      new Itty(Pointer(center.offset(-dx, -dy, -dz), 2*dx, 2*dy, 2*dz))
  }
}
