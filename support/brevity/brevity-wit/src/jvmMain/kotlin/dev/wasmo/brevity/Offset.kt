package dev.wasmo.brevity

data class Offset(
  val line: Int,
  val column: Int,
) {
  override fun toString() = "$line:$column"
}
