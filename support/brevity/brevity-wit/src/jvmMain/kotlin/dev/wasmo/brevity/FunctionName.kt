package dev.wasmo.brevity

import dev.wasmo.brevity.io.UsePath

/**
 * An external function name, used as a unique identifier in a .wasm files.
 */
data class FunctionName(
  val packageName: PackageName? = null,
  val name: Identifier,
  val parentName: Identifier? = null,
  val resourceName: Identifier? = null,
  val annotation: Annotation? = null,
) {
  val moduleName: String?
    get() = when {
      packageName != null && parentName != null -> UsePath(packageName, parentName).toString()
      packageName != null -> packageName.toString()
      else -> null
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

  override fun toString(): String {
    val moduleName = this.moduleName
    return when {
      moduleName != null -> "$moduleName#$abiName"
      else -> abiName
    }
  }
}

enum class Annotation(val value: String) {
  Constructor("[constructor]"),
  Method("[method]"),
  ResourceDrop("[resource-drop]"),
  Static("[static]"),
}
