package com.wasmo.support.wit.io.kotlin

data class Quad<A, B, C, D>(
  val a: A,
  val b: B,
  val c: C,
  val d: D,
) {
  override fun toString() = "($a, $b, $c, $d)"
}

class Borrow<T : Any>(val value: T) {
  override fun equals(other: Any?) = other is Borrow<*> && other.value == value

  override fun hashCode() = value.hashCode()

  override fun toString() = "Borrow($value)"
}

interface Stream<T : Any> {
  fun next(): T
}

