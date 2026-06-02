package com.wasmo.support.wit

object Types {
  val bool = TypeName("bool")
  val s8 = TypeName("s8")
  val s16 = TypeName("s16")
  val s32 = TypeName("s32")
  val s64 = TypeName("s64")
  val u8 = TypeName("u8")
  val u16 = TypeName("u16")
  val u32 = TypeName("u32")
  val u64 = TypeName("u64")
  val f32 = TypeName("f32")
  val f64 = TypeName("f64")
  val char = TypeName("char")
  val string = TypeName("string")
}

sealed class TypeName {
  data class Simple(
    val name: Identifier,
  ) : TypeName() {
    override fun toString() = name.toString()
  }

  data class Tuple(
    val types: kotlin.collections.List<TypeName>,
  ) : TypeName()

  data class List(
    val type: TypeName,
    val size: UInt? = null,
  ) : TypeName()

  data class Option(
    val type: TypeName,
  ) : TypeName()

  data class Borrow(
    val type: TypeName,
  ) : TypeName()

  data class Result(
    val ok: TypeName? = null,
    val err: TypeName? = null,
  ) : TypeName()

  data class Map(
    val key: TypeName,
    val value: TypeName,
  ) : TypeName()

  data class Future(
    val type: TypeName? = null,
  ) : TypeName()

  data class Stream(
    val type: TypeName? = null,
  ) : TypeName()

  companion object {
    operator fun invoke(name: String) = Simple(Identifier(name))
    operator fun invoke(name: Identifier) = Simple(name)
  }
}
