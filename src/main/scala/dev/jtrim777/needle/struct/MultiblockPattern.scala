package dev.jtrim777.needle.struct

import net.minecraft.block.{Block, BlockState, Blocks => VanillaBlocks}
import net.minecraft.util.BlockRotation
import MultiblockPattern._
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import dev.jtrim777.needle.util.syntax._

class MultiblockPattern(val pattern: List[Entry], val symmetrical: Boolean,
                        val allowedRotations: Set[BlockRotation]) {
  private lazy val displayCache: Map[BlockPos, BlockState] =
    pattern.groupBy(_.pos).map(p => (p._1, p._2.head.display))

  private lazy val searchRots: Set[BlockRotation] = {
    val baseline = if (symmetrical) Set(BlockRotation.CLOCKWISE_90) else BlockRotation.values().toSet

    (allowedRotations & baseline) + BlockRotation.NONE
  }

  def test(origin: BlockPos, world: World): Boolean = {
    searchRots.exists(test(origin, world, _))
  }

  def test(origin: BlockPos, world: World, rot: BlockRotation): Boolean = {
    testVerbose(origin, world, rot).isEmpty
  }

  def testVerbose(origin: BlockPos, world: World, rot: BlockRotation): Option[Entry] = {
    this.pattern.find(e => !checkOne(e, origin, world, rot))
  }

  private def checkOne(entry: Entry, origin: BlockPos, world: World, rot: BlockRotation): Boolean = {
    val testPos = entry.pos.rotate(rot).add(origin):BlockPos

    entry.rule(world.getBlockState(testPos), rot)
  }
}

object MultiblockPattern {
  private type PatternDefinition = Array[Array[String]]
  private type AnnotPD = Array[(Array[(IndexedSeq[(Char, Int)], Int)], Int)]
  private type Matcher = (BlockState, BlockRotation) => Boolean
  private case class BadPatternError(msg: String) extends Exception(msg)

  case class Entry(pos: BlockPos, display: BlockState, rule: Matcher, char: Char)

  def apply: Builder = Builder(None, Map.empty, BlockRotation.values().toSet)

  case class Builder private(defn: Option[PatternDefinition],
                             rules: Map[Char, (BlockState, Matcher)],
                             allowedRots: Set[BlockRotation]) {
    def pattern(defn: PatternDefinition): Builder = if (defn.isEmpty) {
      this.copy(defn = Some(defn))
    } else {
      throw new IllegalStateException("A pattern has already been defined on this builder")
    }

    def rule(key: Char, display: BlockState, matcher: Matcher): Builder = {
      if (rules.contains(key)) {
        throw new IllegalStateException(s"This builder already has a rule for key '$key'")
      } else if (key == ' ') {
        throw new IllegalStateException("The key ' ' is reserved to match air blocks")
      } else if (key == '_') {
        throw new IllegalStateException("The key ' ' is reserved to match any block")
      }

      this.copy(rules = rules.updated(key, (display, matcher)))
    }

    def rule(key: Char, display: BlockState, stateMatcher: BlockState => Boolean): Builder = {
      this.rule(key, display, (state, _) => stateMatcher(state))
    }
    def rule(key: Char, display: BlockState, block: Block): Builder = {
      this.rule(key, display, (state, _) => state.isOf(block))
    }

    def restrictRotations(allowed: Set[BlockRotation]): Builder = {
      this.copy(allowedRots = allowed)
    }

    def build(): MultiblockPattern = {
      defn match {
        case Some(value) => MultiblockPattern.parse(value, rules, allowedRots)
        case None => throw new IllegalStateException("The pattern must be defined on this builder")
      }
    }
  }

  private def getDimensions(defn: AnnotPD): (Int, Int, Int) = {
    val yDim = defn.length
    if (yDim == 0) throw BadPatternError("Empty patterns are not allowed")

    val zDim = defn.head._1.length
    if (zDim == 0) throw BadPatternError("The bottom layer of a pattern may not be empty")

    val xDim = defn.head._1.head._1.length
    if (xDim == 0) throw BadPatternError("The first row in a pattern may not be empty")

    defn.foreach { case (layer, y) =>
      if (layer.length != zDim && layer.length != 0) {
        throw BadPatternError(s"Layer $y does not have expected z-dimension $zDim")
      }

      layer.foreach { case (row, z) =>
        if (row.length != xDim && row.nonEmpty) {
          throw BadPatternError(s"Layer $y, row $z does not have expected x-dimension $xDim")
        }
      }
    }

    (xDim, yDim, zDim)
  }

  private def findOrigin(defn: AnnotPD): (Int, Int, Int) = {
    val zeroes = defn.flatMap { case (layer, y) =>
      layer.flatMap { case (row, z) =>
        row.filter(_._1 == '0').map(p => (p._2, y, z))
      }
    }.toList

    zeroes match {
      case Nil => (0, 0, 0)
      case origin :: Nil => origin
      case _ => throw BadPatternError(s"Only one origin character ('0') is allowed per-pattern")
    }
  }

  private def parse(defn: PatternDefinition, dict: Map[Char, (BlockState, Matcher)],
                    allowedRots: Set[BlockRotation]): MultiblockPattern = {
    val annotated: AnnotPD = defn.map(_.map(_.zipWithIndex).zipWithIndex).zipWithIndex

    val _ = getDimensions(annotated)
    val origin = findOrigin(annotated).toBlockPos

    val entries = annotated.flatMap { case (layer, y) =>
      layer.flatMap { case (row, z) =>
        row.flatMap { case (c, x) =>
          c match {
            case ' ' => Some(Entry(new BlockPos(x, y, z).subtract(origin), VanillaBlocks.AIR.getDefaultState, (s, _) => s.isOf(VanillaBlocks.AIR), c))
            case '_' => None
            case _ => dict.get(c) match {
              case Some((disp, rule)) => Some(Entry(new BlockPos(x, y, z).subtract(origin), disp, rule, c))
              case None => throw BadPatternError(s"Definition missing entry for character '$c', " +
                s"used at ($x, $y, $z)")
            }
          }
        }
      }
    }.toList

    // TODO: Add symmetry detection

    new MultiblockPattern(entries, false, allowedRots)
  }
}
