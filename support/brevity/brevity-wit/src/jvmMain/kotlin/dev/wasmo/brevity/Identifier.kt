package dev.wasmo.brevity

@JvmInline
value class Identifier(
  val name: String,
) {
  override fun toString() = name
}
