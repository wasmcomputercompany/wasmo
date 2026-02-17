package com.wasmo.testing

import com.wasmo.api.PasskeyRegistration
import com.wasmo.passkeys.RegistrationRecord

fun PasskeyRegistration.registrationRecord() = RegistrationRecord(
  response.attestationObject,
  response.clientDataJSON,
  response.transports,
)
