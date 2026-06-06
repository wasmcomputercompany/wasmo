package com.wasmo.support.wit.kotlin.generator

import com.squareup.kotlinpoet.ClassName
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.PackageName

internal fun className(
  packagePrefix: String,
  packageName: PackageName?,
  interfaceName: Identifier,
): ClassName {
  val kotlinPackageName = packageName?.toKotlin(packagePrefix) ?: packagePrefix
  return ClassName(kotlinPackageName, interfaceName.name.toCamelCase(upperCamel = true))
}

internal fun className(
  packagePrefix: String,
  packageName: PackageName?,
  interfaceName: Identifier,
  typeName: Identifier,
) = className(packagePrefix, packageName, interfaceName)
  .nestedClass(typeName.name.toCamelCase(upperCamel = true))

internal fun PackageName.toKotlin(prefix: String): String {
  val segments = buildList {
    add(prefix)
    addAll(namespaces.map { it.name.toPackageSegment() })
    addAll(names.map { it.name.toPackageSegment() })
    version?.let {
      add("v${it.version.toPackageSegment()}")
    }
  }
  return segments.joinToString(separator = ".")
}

internal fun String.toPackageSegment(): String {
  return map { char ->
    when (char) {
      in '0'..'9' -> char
      in 'a'..'z' -> char
      in 'A'..'Z' -> char - ('A' - 'a')
      else -> '_'
    }
  }.toCharArray().concatToString()
}

/**
 * Converts a `kabob-case` to `UpperCamelCase` or `lowerCamelCase`.
 */
internal fun String.toCamelCase(upperCamel: Boolean): String {
  return buildString {
    var uppercase = upperCamel
    for (char in this@toCamelCase) {
      when (char) {
        '-' -> {
          uppercase = true
          continue
        }

        in 'a'..'z' if uppercase -> append(char - ('a' - 'A'))
        in 'A'..'Z' if !uppercase -> append(char - ('A' - 'a'))
        else -> append(char)
      }
      uppercase = false
    }
  }
}
