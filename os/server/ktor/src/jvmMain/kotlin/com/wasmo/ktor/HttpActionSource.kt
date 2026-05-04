package com.wasmo.ktor

interface HttpActionSource {
  context(binder: HttpActionBinder)
  fun bindActions()
}
