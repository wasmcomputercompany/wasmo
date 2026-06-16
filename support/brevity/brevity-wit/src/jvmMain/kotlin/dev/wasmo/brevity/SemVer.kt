package dev.wasmo.brevity

@JvmInline
value class SemVer(
  val version: String,
) {
  override fun toString() = version
}
