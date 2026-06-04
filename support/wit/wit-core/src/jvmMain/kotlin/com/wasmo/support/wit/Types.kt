package com.wasmo.support.wit

sealed class TypeName {
  object Bool : TypeName()
  object S8 : TypeName()
  object S16 : TypeName()
  object S32 : TypeName()
  object S64 : TypeName()
  object U8 : TypeName()
  object U16 : TypeName()
  object U32 : TypeName()
  object U64 : TypeName()
  object F32 : TypeName()
  object F64 : TypeName()
  object Char : TypeName()
  object String : TypeName()

  /** Identifies a [TypeDeclaration]. */
  data class Declared(
    val name: Identifier,
  ) : TypeName() {
    override fun toString() = name.toString()

    companion object {
      operator fun invoke(name: kotlin.String) = Declared(Identifier(name))
    }
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
}
