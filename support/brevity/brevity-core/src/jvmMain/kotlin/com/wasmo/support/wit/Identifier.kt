package com.wasmo.support.wit

@JvmInline
value class Identifier(
  val name: String,
) {
  override fun toString() = name
}
