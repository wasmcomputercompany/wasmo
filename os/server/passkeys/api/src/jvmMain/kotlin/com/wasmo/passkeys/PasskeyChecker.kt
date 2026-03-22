package com.wasmo.passkeys

import com.wasmo.api.PasskeyAuthentication
import com.wasmo.api.PasskeyRegistration

interface PasskeyChecker {
  fun register(
    registration: PasskeyRegistration,
  ): RegisterResult

  /** Throws if authentication fails. */
  fun authenticate(
    authentication: PasskeyAuthentication,
    registrationRecord: RegistrationRecord,
  )
}
