package com.wasmo.framework

interface ActionSource {
  /** For precedence; lower items are bound earlier. */
  val order: Int

  context(binder: Binder)
  fun bindActions()

  /**
   * Use this to register HTTP actions at service start up.
   */
  interface Binder {
    fun register(actionRegistration: ActionRegistration)
  }
}
