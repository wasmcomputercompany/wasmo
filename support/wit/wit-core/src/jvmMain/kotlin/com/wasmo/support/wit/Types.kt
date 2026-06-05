package com.wasmo.support.wit

sealed class TypeName {
  data object Bool : TypeName()
  data object S8 : TypeName()
  data object S16 : TypeName()
  data object S32 : TypeName()
  data object S64 : TypeName()
  data object U8 : TypeName()
  data object U16 : TypeName()
  data object U32 : TypeName()
  data object U64 : TypeName()
  data object F32 : TypeName()
  data object F64 : TypeName()
  data object Char : TypeName()
  data object String : TypeName()

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
