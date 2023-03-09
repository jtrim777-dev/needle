package dev.jtrim777.needle.util

import net.minecraft.util.math.{BlockPos, Vec3i}

object syntax {
  implicit class BlockPosOps(bp: BlockPos) {
    def offset(x: Int, y: Int, z: Int): BlockPos = {
      bp.asInstanceOf[Vec3i].add(x, y, z).asInstanceOf[BlockPos]
    }

    def range(dx: Int, dy: Int, dz: Int): PosArea = PosArea.Cubic(bp, dx, dy, dz)
  }
}
