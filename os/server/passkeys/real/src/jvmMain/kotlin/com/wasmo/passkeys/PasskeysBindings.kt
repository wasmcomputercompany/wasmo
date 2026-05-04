package com.wasmo.passkeys

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds

@BindingContainer
abstract class PasskeysBindings {
  @Binds
  internal abstract fun bindAuthenticatorDatabase(
    real: RealAuthenticatorDatabase,
  ): AuthenticatorDatabase
}
