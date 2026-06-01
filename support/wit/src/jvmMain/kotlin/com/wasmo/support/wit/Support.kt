package com.wasmo.support.wit

sealed interface Either<A, B> {
  data class A<A, B>(val value: A) : Either<A, B>
  data class B<A, B>(val value: B) : Either<A, B>
}
