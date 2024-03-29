package dev.jtrim777.needle

import net.minecraft.item.ItemStack
import net.minecraft.nbt._
import net.minecraft.util.Identifier

import scala.jdk.CollectionConverters._
import scala.util.Try


package object nbt {
  def codec[A](encoder: A => NbtElement, decoder: NbtElement => A): NBTCodec[A] = new NBTCodec[A] {
    override def decode(source: NbtElement): A = decoder(source)

    override def encode(value: A): NbtElement = encoder(value)
  }

  implicit class NBTEOps(element: NbtElement) {
    def downCast[X <: NbtElement](typeLabel: NbtType[X]): X = element.getNbtType match {
      case `typeLabel` => element.asInstanceOf[X]
      case _ => throw new ClassCastException(s"$element is a ${element.getNbtType} and cannot be cast to a $typeLabel")
    }

    def as[A : NBTDecoder]: A = implicitly[NBTDecoder[A]].decode(element)

    def asOpt[A : NBTDecoder]: Option[A] = Try(implicitly[NBTDecoder[A]].decode(element)).toOption

    def contains[A : NBTDecoder](value: A): Boolean = asOpt[A].contains(value)

    /**
     * Uses a fuzzy match to determine if two NbtElement's are similar. For non-structured data,
     * equivalence is required. For lists, all elements in the pattern must be contained in the
     * target. For objects, all keys in the pattern must exist in the target and their values
     * must match
     */
    def matches(pattern: NbtElement): Boolean = NBTPredicate.matches(pattern, element)
  }

  implicit class EncodableOps[A : NBTEncoder](a: A) {
    def asNBT: NbtElement = implicitly[NBTEncoder[A]].encode(a)
  }

  implicit val NoopCodec: NBTCodec[NbtElement] = codec({a => a}, {b => b})
  implicit val ByteCodec: NBTCodec[Byte] = codec({i => NbtByte.of(i)}, { e => e.downCast(NbtByte.TYPE).byteValue()})
  implicit val ShortCodec: NBTCodec[Short] = codec({i => NbtShort.of(i)}, { e => e.downCast(NbtShort.TYPE).shortValue()})
  implicit val IntCodec: NBTCodec[Int] = codec({i => NbtInt.of(i)}, {e => e.downCast(NbtInt.TYPE).intValue()})
  implicit val LongCodec: NBTCodec[Long] = codec({i => NbtLong.of(i)}, { e => e.downCast(NbtLong.TYPE).longValue()})
  implicit val FloatCodec: NBTCodec[Float] = codec({i => NbtFloat.of(i)}, { e => e.downCast(NbtFloat.TYPE).floatValue()})
  implicit val DoubleCodec: NBTCodec[Double] = codec({i => NbtDouble.of(i)}, { e => e.downCast(NbtDouble.TYPE).doubleValue()})
  implicit val StringCodec: NBTCodec[String] = codec({i => NbtString.of(i)}, { e => e.downCast(NbtString.TYPE).asString()})
  implicit val BooleanCodec: NBTCodec[Boolean] = codec({i => NbtByte.of(i)}, {e => e.as[Byte] == 1})

  implicit val BArrayEncoder: NBTCodec[Iterable[Byte]] = codec({bs =>new NbtByteArray(bs.toArray)},
    {e => e.downCast(NbtByteArray.TYPE).getByteArray})

  implicit def seqCodec[A : NBTEncoder : NBTDecoder]: NBTCodec[Seq[A]] = codec(
    { is =>
      val nbtList = new NbtList()
      is.foreach(a => nbtList.add(implicitly[NBTEncoder[A]].encode(a)))
      nbtList
    },
    { e =>
      val nlist = e.downCast(NbtList.TYPE)
      (0 until nlist.size()).map(i => (nlist.get(i) : NbtElement).as[A])
    }
  )

  implicit def mapCodec[V : NBTEncoder : NBTDecoder]: NBTCodec[Map[String, V]] = codec(
    { map =>
      val cmpd = new NbtCompound()
      map.foreach(t => cmpd.put(t._1, implicitly[NBTEncoder[V]].encode(t._2)))
      cmpd
    },
    { e =>
      val nmap = e.downCast(NbtCompound.TYPE)
      nmap.getKeys.asScala.toList.map(s => s -> nmap.get(s).as[V]).toMap
    }
  )

  implicit def identMapCodec[V: NBTEncoder : NBTDecoder]: NBTCodec[Map[Identifier, V]] = codec(
    { map =>
      val cmpd = new NbtCompound()
      map.foreach(t => cmpd.put(t._1.toString, implicitly[NBTEncoder[V]].encode(t._2)))
      cmpd
    },
    { e =>
      val nmap = e.downCast(NbtCompound.TYPE)
      nmap.getKeys.asScala.toList.map(s => new Identifier(s) -> nmap.get(s).as[V]).toMap
    }
  )

  private case class EitherWrapper[L : NBTEncoder : NBTDecoder, R : NBTEncoder : NBTDecoder](left: Option[L], right: Option[R]) {
    def unwrap: Either[L, R] = left match {
      case Some(value) => Left(value)
      case None => Right(right.get)
    }
  }
  private object EitherWrapper {
    def wrap[L : NBTEncoder : NBTDecoder, R : NBTEncoder : NBTDecoder](e: Either[L, R]): EitherWrapper[L, R] = e match {
      case Left(value) => EitherWrapper(Some(value), None)
      case Right(value) => EitherWrapper(None, Some(value))
    }
  }

  implicit def eitherCodec[L : NBTEncoder : NBTDecoder, R : NBTEncoder : NBTDecoder]: NBTCodec[Either[L, R]] = {
    import generic._
    codec({a => EitherWrapper.wrap(a).asNBT }, {e => e.as[EitherWrapper[L, R]].unwrap })
  }

  implicit val IdentCodec: NBTCodec[Identifier] = NBTCodec.from(
    StringCodec.contramap(_.toString),
    StringCodec.map(new Identifier(_))
  )

  implicit val ISCodec: NBTCodec[ItemStack] = codec(is => is.writeNbt(new NbtCompound),
    e => ItemStack.fromNbt(e.downCast(NbtCompound.TYPE)))
}
