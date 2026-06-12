package dev.wasmo.brevity

data class FunctionName(
  val packageName: PackageName,
  val name: Identifier,
  val resourceName: Identifier? = null,
  val annotation: Annotation? = null,
) {
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
}

enum class Annotation(val value: String) {
  Constructor("[constructor]"),
  Method("[method]"),
  ResourceDrop("[resource-drop]"),
  Static("[static]"),
}
