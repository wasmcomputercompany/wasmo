package com.wasmo.passkeys

import com.wasmo.api.PasskeyAuthentication
import com.wasmo.api.PasskeyRegistration
import com.webauthn4j.WebAuthnAuthenticationManager
import com.webauthn4j.WebAuthnRegistrationManager
import com.webauthn4j.converter.AttestationObjectConverter
import com.webauthn4j.converter.AuthenticatorDataConverter
import com.webauthn4j.converter.CollectedClientDataConverter
import com.webauthn4j.converter.util.ObjectConverter
import com.webauthn4j.credential.CredentialRecord
import com.webauthn4j.credential.CredentialRecordImpl
import com.webauthn4j.data.AuthenticationData
import com.webauthn4j.data.AuthenticationParameters
import com.webauthn4j.data.AuthenticatorTransport
import com.webauthn4j.data.PublicKeyCredentialParameters
import com.webauthn4j.data.PublicKeyCredentialType
import com.webauthn4j.data.RegistrationData
import com.webauthn4j.data.RegistrationParameters
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier
import com.webauthn4j.data.client.Origin
import com.webauthn4j.data.extension.authenticator.AuthenticationExtensionAuthenticatorOutput
import com.webauthn4j.server.ServerProperty
import okhttp3.HttpUrl
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString

/**
 * Interact with WebAuthn4J to register and authenticate passkeys.
 *
 * https://github.com/webauthn4j/webauthn4j/
 */
class RealPasskeyChecker(
  private val challenger: Challenger,
  private val baseUrl: HttpUrl,
) : PasskeyChecker {
  private val objectConverter = ObjectConverter()
  private val collectedClientDataConverter = CollectedClientDataConverter(objectConverter)
  private val attestationObjectConverter = AttestationObjectConverter(objectConverter)
  private val authenticatorDataConverter = AuthenticatorDataConverter(objectConverter)

  private val registrationManager = WebAuthnRegistrationManager
    .createNonStrictWebAuthnRegistrationManager(objectConverter)
  private val authenticationManager = WebAuthnAuthenticationManager(listOf(), objectConverter)

  override fun register(registration: PasskeyRegistration): RegisterResult {
    val attestationObjectBytes = registration.response.attestationObject.toByteArray()
    val attestationObject = attestationObjectConverter.convert(attestationObjectBytes)
      ?: throw Exception("failed to decode attestationObject")

    val attestedCredentialData = attestationObject.authenticatorData.attestedCredentialData
      ?: throw Exception("failed to decode attestedCredentialData")

    val clientDataBytes = registration.response.clientDataJSON
    val clientData = collectedClientDataConverter.convert(clientDataBytes.toByteArray())
      ?: throw Exception("failed to decode client data")
    val challengeBytes = clientData.challenge.value

    challenger.check(challengeBytes.toByteString())

    val serverProperty = ServerProperty.builder()
      .origin(Origin.create(baseUrl.toString()))
      .challenge { challengeBytes }
      .rpId(baseUrl.host)
      .build()

    val registrationData = RegistrationData(
      attestationObject,
      attestationObjectBytes,
      clientData,
      clientDataBytes.toByteArray(),
      null,
      registration.response.transports.map { AuthenticatorTransport.create(it) }.toSet(),
    )

    val registrationParameters = RegistrationParameters(
      serverProperty,
      listOf(
        PublicKeyCredentialParameters(
          PublicKeyCredentialType.create(registration.type),
          COSEAlgorithmIdentifier.create(registration.response.publicKeyAlgorithm.toLong()),
        ),
      ),
      false,
      false,
    )

    registrationManager.verify(
      registrationData,
      registrationParameters,
    )

    return RegisterResult(
      id = attestedCredentialData.credentialId.toByteString().base64Url().trimEnd('='),
      aaguid = attestedCredentialData.aaguid.value.toString(),
      record = RegistrationRecord(
        attestationObject = registration.response.attestationObject,
        clientDataJSON = registration.response.clientDataJSON,
        transports = registration.response.transports,
      ),
    )
  }

  override fun authenticate(
    authentication: PasskeyAuthentication,
    registrationRecord: RegistrationRecord,
  ) {
    val authenticatorDataBytes = authentication.response.authenticatorData.toByteArray()
    val authenticatorData = authenticatorDataConverter
      .convert<AuthenticationExtensionAuthenticatorOutput>(authenticatorDataBytes)

    val clientDataBytes = authentication.response.clientDataJSON
    val clientData = collectedClientDataConverter.convert(clientDataBytes.toByteArray())
      ?: throw Exception("failed to decode client data")

    val challengeBytes = clientData.challenge.value
    challenger.check(challengeBytes.toByteString())

    val authenticationData = AuthenticationData(
      authentication.id.toByteArray(),
      null,
      authenticatorData,
      authenticatorDataBytes,
      clientData,
      clientDataBytes.toByteArray(),
      null,
      authentication.response.signature.toByteArray(),
    )

    val serverProperty = ServerProperty.builder()
      .origin(Origin.create(baseUrl.toString()))
      .challenge { challengeBytes }
      .rpId(baseUrl.host)
      .build()

    val authenticationParameters = AuthenticationParameters(
      serverProperty,
      registrationRecord.toCredentialRecord(),
      listOf(authentication.id.encodeUtf8().toByteArray()),
      false,
      false,
    )

    authenticationManager.verify(
      authenticationData,
      authenticationParameters,
    )
  }

  private fun RegistrationRecord.toCredentialRecord(): CredentialRecord {
    val attestationObject = attestationObjectConverter
      .convert(attestationObject.toByteArray())
      ?: throw Exception("failed to decode attestation object")
    return CredentialRecordImpl(
      attestationObject,
      collectedClientDataConverter.convert(clientDataJSON.toByteArray()),
      null,
      transports.map { AuthenticatorTransport.create(it) }.toSet(),
    )
  }
}
