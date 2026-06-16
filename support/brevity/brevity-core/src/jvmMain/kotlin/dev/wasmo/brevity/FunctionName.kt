package dev.wasmo.brevity

import dev.wasmo.brevity.io.UsePath

/**
 * An external function name, used as a unique identifier in a .wasm files.
 */
data class FunctionName(
  val packageName: PackageName,
  val name: Identifier,
  val parentName: Identifier? = null,
  val resourceName: Identifier? = null,
  val annotation: Annotation? = null,
) {
  val moduleName: String
    get() = when {
      parentName != null -> UsePath(packageName, parentName).toString()
      else -> packageName.toString()
    }

  val abiName: String
    get() = buildString {
      if (annotation != null) {
        append(annotation.value)
      }
      if (resourceName != null) {
        append(resourceName.name)
        append('.')
      }
      append(name.name)
    }

  override fun toString() = "$moduleName#$abiName"
}

enum class Annotation(val value: String) {
  Constructor("[constructor]"),
  Method("[method]"),
  ResourceDrop("[resource-drop]"),
  Static("[static]"),
}
