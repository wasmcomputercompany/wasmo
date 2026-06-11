package com.wasmo.support.wit

@JvmInline
value class SemVer(
  val version: String,
) {
  override fun toString() = version
}
