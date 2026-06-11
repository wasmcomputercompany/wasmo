package com.wasmo.support.wit.io

import com.wasmo.support.wit.Identifier

sealed class IoTypeName {
  data object Bool : IoTypeName()
  data object S8 : IoTypeName()
  data object S16 : IoTypeName()
  data object S32 : IoTypeName()
  data object S64 : IoTypeName()
  data object U8 : IoTypeName()
  data object U16 : IoTypeName()
  data object U32 : IoTypeName()
  data object U64 : IoTypeName()
  data object F32 : IoTypeName()
  data object F64 : IoTypeName()
  data object Char : IoTypeName()
  data object String : IoTypeName()

  /** Identifies a [IoTypeDeclaration]. */
  data class Declared(
    val name: Identifier,
  ) : IoTypeName() {
    override fun toString() = name.toString()

    companion object {
      operator fun invoke(name: kotlin.String) = Declared(Identifier(name))
    }
  }

  data class Tuple(
    val types: kotlin.collections.List<IoTypeName>,
  ) : IoTypeName()

  data class List(
    val type: IoTypeName,
    val size: UInt? = null,
  ) : IoTypeName()

  data class Option(
    val type: IoTypeName,
  ) : IoTypeName()

  data class Borrow(
    val type: IoTypeName,
  ) : IoTypeName()

  data class Result(
    val ok: IoTypeName? = null,
    val err: IoTypeName? = null,
  ) : IoTypeName()

  data class Map(
    val key: IoTypeName,
    val value: IoTypeName,
  ) : IoTypeName()

  data class Future(
    val type: IoTypeName? = null,
  ) : IoTypeName()

  data class Stream(
    val type: IoTypeName? = null,
  ) : IoTypeName()
}
