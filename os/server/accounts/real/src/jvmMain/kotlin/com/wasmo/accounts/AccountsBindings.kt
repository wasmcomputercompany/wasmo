package com.wasmo.accounts

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds

@BindingContainer
abstract class AccountsBindings {
  @Binds
  abstract fun bindClientAuthenticatorFactory(
    real: RealClientAuthenticator.Factory,
  ): ClientAuthenticator.Factory
}
