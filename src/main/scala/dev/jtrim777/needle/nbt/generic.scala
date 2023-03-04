package dev.jtrim777.needle.nbt

import net.minecraft.nbt.{NbtCompound, NbtElement}
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}
import shapeless.labelled.{FieldType, field}

object generic {
  implicit val hnilCodec: NBTCodec[HNil] = codec(
    { _ => new NbtCompound() },
    { _ => HNil }
  )

//  implicit val cnilCodec: NBTCodec[CNil] = codec(
//    { _ => throw new Exception("Impossible state reached") },
//    { _ => throw new Exception("Impossible state reached") }
//  )

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
      tail
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

  implicit def hlistOOEncoder[K <: Symbol, H, T <: HList](implicit
                                                        witness: Witness.Aux[K],
                                                        hEncoder: Lazy[NBTEncoder[H]],
                                                        tEncoder: NBTEncoder[T]
                                                       ): NBTEncoder[FieldType[K, Option[H]] :: T] = {
    val fieldName = witness.value.name

    { (input: FieldType[K, Option[H]] :: T) =>
      val tail = tEncoder.encode(input.tail).downCast(NbtCompound.TYPE)

      input.head.foreach(h => tail.put(fieldName, hEncoder.value.encode(h)))
      tail
    }
  }

  implicit def hlistOODecoder[K <: Symbol, H, T <: HList](implicit
                                                        witness: Witness.Aux[K],
                                                        hDecoder: Lazy[NBTDecoder[H]],
                                                        tDecoder: NBTDecoder[T]
                                                       ): NBTDecoder[FieldType[K, Option[H]] :: T] = {
    val fieldName = witness.value.name

    { (source: NbtElement) =>
      val cmpd = source.downCast(NbtCompound.TYPE)
      val head = if (cmpd.contains(fieldName)) Some(hDecoder.value.decode(cmpd.get(fieldName))) else None
      val tail = tDecoder.decode(source)
      field[K](head) :: tail
    }
  }

//  implicit def coproductEncoder[K <: Symbol, H, T <: Coproduct](implicit
//                                                               witness: Witness.Aux[K],
//                                                                hEncoder: Lazy[NBTEncoder[H]],
//                                                                tEncoder: NBTEncoder[T]
//                                                               ): NBTEncoder[FieldType[K, H] :+: T] = {
//    val typeName = witness.value.name
//
//    {
//      case Inl(head) => new NbtCompound().put(typeName, hEncoder.value.encode(head))
//      case Inr(tail) => tEncoder.encode(tail)
//    }
//  }

//  implicit def coproductDecoder[K <: Symbol, H, T <: Coproduct](implicit
//                                                                witness: Witness.Aux[K],
//                                                                hEncoder: NBTDecoder[H],
//                                                                tEncoder: Lazy[NBTDecoder[T]]
//                                                               ): NBTDecoder[FieldType[K, H] :+: T] = {
//    val typeName = witness.value.name
//
//    { e:NbtElement =>
//      val cmpd = e.downCast(NbtCompound.TYPE)
//
//      if (cmpd.contains(typeName)) {
//        Inl(field[K](hEncoder.decode(cmpd.get(typeName))))
//      } else Inr(tEncoder.value.decode(cmpd))
//    }
//  }


  implicit def genericEncoder[A, H](implicit generic: LabelledGeneric.Aux[A, H],
                                    hcoder: Lazy[NBTEncoder[H]]): NBTEncoder[A] = { a => hcoder.value.encode(generic.to(a)) }

  implicit def genericDecoder[A, H](implicit generic: LabelledGeneric.Aux[A, H],
                                    hcoder: Lazy[NBTDecoder[H]]): NBTDecoder[A] = { e =>
    generic.from(hcoder.value.decode(e))
  }
}
