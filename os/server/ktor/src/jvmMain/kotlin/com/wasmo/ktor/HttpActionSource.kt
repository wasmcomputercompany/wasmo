package com.wasmo.ktor

import com.wasmo.framework.HttpActionBinder

interface HttpActionSource {
  val order: Int

  context(binder: HttpActionBinder)
  fun bindActions()
}
