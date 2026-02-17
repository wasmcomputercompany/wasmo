package com.wasmo.passkeys

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.FakeClock
import com.wasmo.testing.FakePasskey
import com.wasmo.testing.registrationRecord
import com.webauthn4j.verifier.exception.BadSignatureException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString.Companion.encodeUtf8

class RealPasskeyCheckerTest {
  private val clock = FakeClock()
  private val cookieToken = "1234567890123456789012345"
  private val challengerFactory = HmacChallenger.Factory(clock, "secret".encodeUtf8())
  private val challenger = challengerFactory.create(cookieToken)
  private val baseUrl = "https://rounds.app/".toHttpUrl()
  private val origin = baseUrl.toString()

  private val passkey = FakePasskey(
    rpId = baseUrl.host,
    id = "passkey-1".encodeUtf8().base64Url(),
    aaguid = AuthenticatorDatabase.ApplePasswords,
  )

  private val passkeyChecker = RealPasskeyChecker(challenger, baseUrl)

  @Test
  fun validRegistration() {
    val registration = passkey.registration(challenger.create(), origin)
    val result = passkeyChecker.register(registration)
    assertThat(result).isEqualTo(
      RegisterResult(
        passkey.id,
        passkey.aaguid,
        RegistrationRecord(
          attestationObject = registration.response.attestationObject,
          clientDataJSON = registration.response.clientDataJSON,
          transports = registration.response.transports,
        ),
      ),
    )
  }

  @Test
  fun registrationWithBadChallenge() {
    val wrongChallenger = challengerFactory.create("XXXXXXXXXXXXXXXXXXXXXXXXX")
    val badChallenge = wrongChallenger.create()

    assertFailsWith<UnexpectedChallengeException> {
      passkeyChecker.register(
        passkey.registration(badChallenge, origin),
      )
    }
  }

  @Test
  fun validAuthentication() {
    val registration = passkey.registration(challenger.create(), origin)
    val registrationRecord = registration.registrationRecord()

    val authentication = passkey.authentication(challenger.create(), origin)

    passkeyChecker.authenticate(
      authentication = authentication,
      registrationRecord = registrationRecord,
    )
  }

  /** Use another passkey's signature. */
  @Test
  fun signatureDoesNotMatch() {
    val registration = passkey.registration(challenger.create(), origin)
    val registrationRecord = registration.registrationRecord()

    val wrongPasskey = FakePasskey(
      rpId = baseUrl.host,
      id = "passkey-2".encodeUtf8().base64Url(),
      aaguid = AuthenticatorDatabase.ApplePasswords,
    )

    val authentication = wrongPasskey.authentication(challenger.create(), origin)

    assertFailsWith<BadSignatureException> {
      passkeyChecker.authenticate(
        authentication = authentication,
        registrationRecord = registrationRecord,
      )
    }
  }

  /** Use another account's challenge. */
  @Test
  fun authenticationWithBadChallenge() {
    val registration = passkey.registration(challenger.create(), origin)
    val registrationRecord = registration.registrationRecord()

    val wrongChallenger = challengerFactory.create("XXXXXXXXXXXXXXXXXXXXXXXXX")
    val badChallenge = wrongChallenger.create()
    val authentication = passkey.authentication(badChallenge, origin)

    assertFailsWith<UnexpectedChallengeException> {
      passkeyChecker.authenticate(
        authentication = authentication,
        registrationRecord = registrationRecord,
      )
    }
  }
}
