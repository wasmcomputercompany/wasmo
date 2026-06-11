package com.wasmo.support.wit.ir

import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.PackageName

sealed class IrTypeName {
  data object Bool : IrTypeName()
  data object S8 : IrTypeName()
  data object S16 : IrTypeName()
  data object S32 : IrTypeName()
  data object S64 : IrTypeName()
  data object U8 : IrTypeName()
  data object U16 : IrTypeName()
  data object U32 : IrTypeName()
  data object U64 : IrTypeName()
  data object F32 : IrTypeName()
  data object F64 : IrTypeName()
  data object Char : IrTypeName()
  data object String : IrTypeName()

  /** Identifies a [IrTypeDeclaration]. */
  data class Declared(
    val packageName: PackageName,
    val interfaceName: Identifier,
    val name: Identifier,
  ) : IrTypeName() {
    override fun toString() = buildString {
      for (namespace in packageName.namespaces) {
        append(namespace)
        append(':')
      }
      for (packageName in packageName.names) {
        append(packageName)
        append('/')
      }
      append(interfaceName)
      if (packageName.version != null) {
        append('@')
        append(packageName.version)
      }
      append(".{$name}")
    }
  }

  data class Tuple(
    val types: kotlin.collections.List<IrTypeName>,
  ) : IrTypeName()

  data class List(
    val type: IrTypeName,
    val size: UInt? = null,
  ) : IrTypeName()

  data class Option(
    val type: IrTypeName,
  ) : IrTypeName()

  data class Borrow(
    val type: IrTypeName,
  ) : IrTypeName()

  data class Result(
    val ok: IrTypeName? = null,
    val err: IrTypeName? = null,
  ) : IrTypeName()

  data class Map(
    val key: IrTypeName,
    val value: IrTypeName,
  ) : IrTypeName()

  data class Future(
    val type: IrTypeName? = null,
  ) : IrTypeName()

  data class Stream(
    val type: IrTypeName? = null,
  ) : IrTypeName()
}
