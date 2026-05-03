package com.wasmo.api

enum class AccountType(val supportedAuthenticationMethods: Set<AuthenticationMethod>) {
  Local(setOf(AuthenticationMethod.Password)),
  Standard(setOf(AuthenticationMethod.EmailAddress, AuthenticationMethod.Passkey)),
  ;
}
