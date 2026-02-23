package com.wasmo.passkeys

import com.wasmo.api.PasskeyAuthentication
import com.wasmo.api.PasskeyRegistration
import okio.ByteString

/**
 * Get a passkey JSON value from the client, call the server, and display a result.
 */
interface PasskeyAuthenticator{
  suspend fun register(user: String, challenge: ByteString): PasskeyRegistration
  suspend fun authenticate(challenge: ByteString): PasskeyAuthentication
}
