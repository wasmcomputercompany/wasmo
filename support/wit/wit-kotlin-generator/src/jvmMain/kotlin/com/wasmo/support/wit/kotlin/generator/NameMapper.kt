package com.wasmo.support.wit.kotlin.generator

import com.squareup.kotlinpoet.ClassName
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.PackageName
import com.wasmo.support.wit.ir.IrTypeName

fun PackageName.toNameMapper(prefix: String): NameMapper.Package {
  val segments = buildList {
    add(prefix)
    addAll(namespaces.map { it.name.toPackageSegment() })
    addAll(names.map { it.name.toPackageSegment() })
    version?.let {
      add("v${it.version.toPackageSegment()}")
    }
  }
  return NameMapper.Package(segments.joinToString(separator = "."))
}

fun IrTypeName.Declared.toNameMapper(prefix: String): NameMapper.Class =
  packageName.toNameMapper(prefix) + interfaceName + name

private fun String.toPackageSegment(): String {
  return map { char ->
    when (char) {
      in '0'..'9' -> char
      in 'a'..'z' -> char
      in 'A'..'Z' -> char - ('A' - 'a')
      else -> '_'
    }
  }.toCharArray().concatToString()
}

/** Maps type names in WIT to type names in Kotlin. */
sealed interface NameMapper {
  /** Appends [identifier] to the end of this name. */
  operator fun plus(identifier: Identifier): Class

  class Package(
    val packageName: String,
  ) : NameMapper {
    override fun plus(identifier: Identifier) =
      Class(ClassName(packageName, identifier.name.toCamelCase(upperCamel = true)))
  }

  class Class(
    val className: ClassName,
  ) : NameMapper {
    override fun plus(identifier: Identifier) =
      Class(className.nestedClass(identifier.name.toCamelCase(upperCamel = true)))
  }
}
