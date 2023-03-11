package dev.jtrim777.needle.util

import net.minecraft.util.Identifier
import net.minecraft.util.math.{BlockPos, Vec3i}

object syntax {
  implicit class BlockPosOps(bp: BlockPos) {
    def offset(x: Int, y: Int, z: Int): BlockPos = {
      bp.asInstanceOf[Vec3i].add(x, y, z).asInstanceOf[BlockPos]
    }

    def range(dx: Int, dy: Int, dz: Int): PosArea = PosArea.Cubic(bp, dx, dy, dz)
  }

  implicit class IdHelper(val sc: StringContext) extends AnyVal {
    def id(args: Any*)(implicit ctx: ModContext): Identifier = {
      val full = sc.s(args)

      val (namespace, name) = if (full.contains(':')) {
        val pts = full.split(':')
        if (pts.length == 2) {
          (pts.head, pts(1))
        } else {
          throw new IllegalArgumentException(s"Illegal identifier format '$full'")
        }
      } else {
        (ctx.namespace, full)
      }

      new Identifier(namespace, name)
    }

    def vanilla(args: Any*): Identifier = {
      val full = sc.s(args)

      new Identifier("minecraft", full)
    }
  }
}
