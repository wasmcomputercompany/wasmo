package com.wasmo.passwords

import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock

@BindingContainer
abstract class PasswordBindings {
  @Binds
  abstract fun bindPasswordHasher(passwordHasher: Argon2PasswordHasher): PasswordHasher

  companion object {
    @Provides
    @SingleIn(OsScope::class)
    fun providePasswordManager(
      clock: Clock,
      passwordHasher: PasswordHasher,
    ): PasswordStore =
      RealPasswordStore(clock, passwordHasher)
  }

}
