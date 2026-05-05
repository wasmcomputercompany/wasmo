package com.wasmo.accounts.passkeys

import com.wasmo.framework.UserAgent

interface PasskeyActions {
  val authenticatePasskeyRpc: AuthenticatePasskeyRpc
  val registerPasskeyRpc: RegisterPasskeyRpc

  interface Factory {
    fun create(userAgent: UserAgent): PasskeyActions
  }
}
