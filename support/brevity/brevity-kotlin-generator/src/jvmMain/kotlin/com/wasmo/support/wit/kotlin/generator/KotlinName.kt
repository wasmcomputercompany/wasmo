package com.wasmo.support.wit.kotlin.generator

import com.squareup.kotlinpoet.ClassName
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.PackageName
import com.wasmo.support.wit.ir.IrTypeName

/** Maps type names in WIT to type names in Kotlin. */
sealed interface KotlinName {
  /** Appends [identifier] to the end of this name. */
  operator fun plus(identifier: Identifier): Class

  class Package(
    val name: String,
  ) : KotlinName {
    override fun plus(identifier: Identifier) =
      Class(ClassName(name, identifier.name.toCamelCase(upperCamel = true)))
  }

  class Class(
    val name: ClassName,
  ) : KotlinName {
    override fun plus(identifier: Identifier) =
      Class(name.nestedClass(identifier.name.toCamelCase(upperCamel = true)))
  }
}

fun PackageName.toKotlin(prefix: String): KotlinName.Package {
  val segments = buildList {
    add(prefix)
    addAll(namespaces.map { it.name.toPackageSegment() })
    addAll(names.map { it.name.toPackageSegment() })
    version?.let {
      add("v${it.version.toPackageSegment()}")
    }
  }
  return KotlinName.Package(segments.joinToString(separator = "."))
}

fun IrTypeName.Declared.toKotlin(prefix: String): KotlinName.Class =
  packageName.toKotlin(prefix) + parentName + name

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
