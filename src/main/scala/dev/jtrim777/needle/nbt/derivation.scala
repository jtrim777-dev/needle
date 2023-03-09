package dev.jtrim777.needle.nbt

import generic._

object derivation {
  def deriveEncoder[A](implicit instance: NBTEncoder[A]): NBTEncoder[A] = instance
  def deriveDecoder[A](implicit instance: NBTDecoder[A]): NBTDecoder[A] = instance
  def deriveCoder[A](implicit enc: NBTEncoder[A], dec: NBTDecoder[A]): NBTCodec[A] = NBTCodec.from(enc, dec)
}
