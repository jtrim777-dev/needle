package dev.jtrim777.needle

package object nbt {
  type NBTCodec[A] = NBTEncoder[A] with NBTDecoder[A]
}
