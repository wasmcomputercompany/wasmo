package com.wasmo.passkeys


import com.wasmo.api.PasskeyAuthentication
import com.wasmo.api.PasskeyRegistration
import com.wasmo.api.WasmoJson
import kotlin.js.Promise
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import kotlinx.serialization.json.decodeFromDynamic
import okio.ByteString

class RealPasskeyAuthenticator : PasskeyAuthenticator {
  override suspend fun register(user: String, challenge: ByteString): PasskeyRegistration {
    val registerArgs = RegisterArgs(
      user = user,
      challenge = challenge.base64Url(),
    )

    val promise = Webauthn.client.register(registerArgs)
    val registration = promise.await()
    return WasmoJson.decodeFromDynamic<PasskeyRegistration>(registration)
  }

  override suspend fun authenticate(challenge: ByteString): PasskeyAuthentication {
    val authenticateArgs = AuthenticateArgs(challenge = challenge.base64Url())
    val promise = Webauthn.client.authenticate(authenticateArgs)
    val authentication = promise.await()
    return WasmoJson.decodeFromDynamic<PasskeyAuthentication>(authentication)
  }
}

@JsModule("@passwordless-id/webauthn")
private external object Webauthn {
  object client {
    /**
     * https://webauthn.passwordless.id/registration/
     */
    fun register(args: RegisterArgs): Promise<dynamic>

    /**
     * https://webauthn.passwordless.id/authentication/
     */
    fun authenticate(args: AuthenticateArgs): Promise<dynamic>
  }
}

@JsPlainObject
private external interface RegisterArgs {
  val user: String
  val challenge: String
}

@JsPlainObject
private external interface AuthenticateArgs {
  val challenge: String
}
