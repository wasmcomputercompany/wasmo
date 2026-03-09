package com.wasmo.framework

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class UserException(message: String?) : Exception(message)

class NotFoundUserException(message: String = "not found") : UserException(message)

/** Thrown when a call fails because of a precondition in the subject state. */
class StateUserException(message: String) : UserException(message)

/** Thrown when the user's argument is invalid. */
class ArgumentUserException(message: String) : UserException(message)

/** Thrown when the user's is not authorized to perform this action. */
class UnauthorizedUserException(message: String = "unauthorized") : UserException(message)

@OptIn(ExperimentalContracts::class)
fun requireUser(value: Boolean, lazyMessage: () -> String) {
  contract {
    returns() implies value
  }
  if (!value) {
    throw ArgumentUserException(lazyMessage())
  }
}

@OptIn(ExperimentalContracts::class)
fun checkUser(value: Boolean, lazyMessage: () -> String) {
  contract {
    returns() implies value
  }
  if (!value) {
    throw StateUserException(lazyMessage())
  }
}
