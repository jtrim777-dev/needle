package dev.jtrim777.needle.nbt

import net.minecraft.nbt.{NbtCompound, NbtElement}
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}
import shapeless.labelled.{FieldType, field}

object generic {
  implicit val hnilCodec: NBTCodec[HNil] = codec(
    { _ => mapCodec[String].encode(Map.empty) },
    { _ => HNil }
  )

  implicit def hlistEncoder[K <: Symbol, H, T <: HList](implicit
                                                        witness: Witness.Aux[K],
                                                        hEncoder: Lazy[NBTEncoder[H]],
                                                        tEncoder: NBTEncoder[T]
                                                       ): NBTEncoder[FieldType[K, H] :: T] = {
    val fieldName = witness.value.name

    { (input: FieldType[K, H] :: T) =>
      val head = hEncoder.value.encode(input.head)
      val tail = tEncoder.encode(input.tail)
      tail.downCast(NbtCompound.TYPE).put(fieldName, head)
    }
  }

  implicit def hlistDecoder[K <: Symbol, H, T <: HList](implicit
                                                        witness: Witness.Aux[K],
                                                        hDecoder: Lazy[NBTDecoder[H]],
                                                        tDecoder: NBTDecoder[T]
                                                       ): NBTDecoder[FieldType[K, H] :: T] = {
    val fieldName = witness.value.name

    { (source: NbtElement) =>
      val cmpd = source.downCast(NbtCompound.TYPE)
      val head = hDecoder.value.decode(cmpd.get(fieldName))
      val tail = tDecoder.decode(source)
      field[K](head) :: tail
    }
  }

  implicit def genericEncoder[A, H](implicit generic: LabelledGeneric.Aux[A, H],
                                    hcoder: Lazy[NBTEncoder[H]]): NBTEncoder[A] = { a => hcoder.value.encode(generic.to(a)) }

  implicit def genericDecoder[A, H](implicit generic: LabelledGeneric.Aux[A, H],
                                    hcoder: Lazy[NBTDecoder[H]]): NBTDecoder[A] = { e =>
    generic.from(hcoder.value.decode(e))
  }
}
