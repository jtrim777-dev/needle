package dev.jtrim777.needle.inv

import dev.jtrim777.needle.nbt.{NBTDecoder, NBTEncoder, seqCodec}
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.{Inventory => MInventory}
import net.minecraft.item.{Item, ItemStack}

trait Inventory extends MInventory with Iterable[ItemStack] {
  def size: Int
  def isEmpty: Boolean
  def getStack(slot: Int): ItemStack
  def setStack(slot: Int, stack: ItemStack): Unit
  def duplicate(): Inventory

  override def removeStack(slot: Int, amount: Int): ItemStack = {
    val stack = getStack(slot)

    val (out: ItemStack, upd: ItemStack) = if (stack.getCount > amount) {
      stack.decrement(amount)
      (stack.copy().setCount(amount), stack)
    } else (stack, ItemStack.EMPTY)

    setStack(slot, upd)
    out
  }

  override def removeStack(slot: Int): ItemStack = {
    val rez = getStack(slot)
    setStack(slot, ItemStack.EMPTY)
    rez
  }

  override def clear(): Unit = {
    (0 until size).foreach(i => setStack(i, ItemStack.EMPTY))
  }

  override def markDirty(): Unit = ()

  override def canPlayerUse(player: PlayerEntity): Boolean = true

  def onUpdate(slot: Int, old: ItemStack, updated: ItemStack): Unit = ()

  def apply(i: Int): ItemStack = getStack(i)
  def update(i: Int, stack: ItemStack): Unit = setStack(i, stack)

  override def iterator: Iterator[ItemStack] = new Inventory.Iter(this)

  def availableSlot(forItem: Item): Option[Int] = {
    this.zipWithIndex.find { case (is, slot) =>
      if (is.isEmpty) {
        isValid(slot, new ItemStack(forItem))
      } else if (is.getItem == forItem) {
        is.getCount < forItem.getMaxCount
      } else false
    }.map(_._2)
  }

  def availableSlots(forItem: Item): Iterable[Int] = {
    this.zipWithIndex.filter { case (is, slot) =>
      if (is.isEmpty) {
        isValid(slot, new ItemStack(forItem))
      } else if (is.getItem == forItem) {
        is.getCount < forItem.getMaxCount
      } else false
    }.map(_._2)
  }

  def insert(stack: ItemStack, slot: Int, errOnIncompat: Boolean = false): ItemStack = {
    val current = this(slot)

    if (stack.isEmpty) {
      stack
    } else if (current.isEmpty) {
      if (isValid(slot, stack)) {
        this(slot) = stack
        ItemStack.EMPTY
      } else if (errOnIncompat) {
        throw new IllegalArgumentException(s"Cannot insert stack of ${stack.getItem} into slot $slot which does not accept that item type")
      } else stack
    } else if (current.isOf(stack.getItem)) {
      val addable = math.min(stack.getCount, current.getMaxCount - current.getCount)
      current.increment(addable)
      val out = stack.copy()
      out.decrement(addable)
      out
    } else {
      if (errOnIncompat) {
        throw new IllegalArgumentException(s"Cannot insert stack of ${stack.getItem} into slot containing ${current.getItem}")
      } else stack
    }
  }

  def insert(stack: ItemStack): ItemStack = {
    if (stack.isEmpty) stack else {
      val available = availableSlots(stack.getItem)

      available.foldLeft(stack) { (rem, slot) =>
        if (rem.isEmpty) rem else {
          this.insert(rem, slot)
        }
      }
    }
  }
}

object Inventory {
  private class Iter(inv: Inventory) extends Iterator[ItemStack] {
    private var i: Int = 0

    override def hasNext: Boolean = i < inv.size

    override def next(): ItemStack = {
      i = i + 1
      inv(i - 1)
    }
  }

  case class Basic(var items: IndexedSeq[ItemStack]) extends Inventory {
    override def size: Int = items.length

    override def isEmpty: Boolean = items.isEmpty

    override def getStack(slot: Int): ItemStack = items(slot)

    override def setStack(slot: Int, stack: ItemStack): Unit = {
      val old = this(slot)
      onUpdate(slot, old, stack)
      this.items = this.items.updated(slot, stack)
    }

    override def duplicate(): Inventory = this.copy(items = items.map(_.copy()))
  }
  object Basic {
    def apply(size: Int): Basic = new Basic(IndexedSeq.fill(size)(ItemStack.EMPTY))
  }

  private case class Wrapper(underlying: MInventory) extends Inventory {
    override def size: Int = underlying.size()

    override def isEmpty: Boolean = underlying.isEmpty

    override def getStack(slot: Int): ItemStack = underlying.getStack(slot)

    override def setStack(slot: Int, stack: ItemStack): Unit = underlying.setStack(slot, stack)

    override def duplicate(): Inventory = Basic(IndexedSeq.from((0 until underlying.size()).map(i => underlying.getStack(i).copy())))
  }
  def from(minecraft: MInventory): Inventory = Wrapper(minecraft)

  implicit val InvEncoder: NBTEncoder[Inventory] = seqCodec[ItemStack].contramap(_.toSeq)
  implicit val BasicDecoder: NBTDecoder[Inventory.Basic] = seqCodec[ItemStack].map(is => Basic(is.toIndexedSeq))
}
