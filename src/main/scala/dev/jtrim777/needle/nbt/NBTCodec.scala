package dev.jtrim777.needle.nbt
import net.minecraft.nbt.NbtElement

trait NBTCodec[A] extends NBTEncoder[A] with NBTDecoder[A] {

}

object NBTCodec {
  def apply[A](implicit instance: NBTCodec[A]): NBTCodec[A] = instance

  def from[A](enc: NBTEncoder[A], dec: NBTDecoder[A]): NBTCodec[A] = new NBTCodec[A] {
    override def encode(value: A): NbtElement = enc.encode(value)

    override def decode(source: NbtElement): A = dec.decode(source)
  }
}
