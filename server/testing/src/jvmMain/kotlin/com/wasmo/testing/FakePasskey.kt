package com.wasmo.testing

import com.wasmo.api.PasskeyAuthentication
import com.wasmo.api.PasskeyRegistration
import com.wasmo.passkeys.RealAuthenticatorDatabase
import com.webauthn4j.converter.AuthenticatorDataConverter
import com.webauthn4j.converter.util.ObjectConverter
import com.webauthn4j.data.attestation.AttestationObject
import com.webauthn4j.data.attestation.authenticator.AAGUID
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData
import com.webauthn4j.data.attestation.authenticator.AuthenticatorData
import com.webauthn4j.data.attestation.authenticator.COSEKey
import com.webauthn4j.data.attestation.authenticator.Curve
import com.webauthn4j.data.attestation.authenticator.EC2COSEKey
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier
import com.webauthn4j.data.attestation.statement.NoneAttestationStatement
import com.webauthn4j.data.extension.authenticator.AuthenticationExtensionsAuthenticatorOutputs
import com.webauthn4j.data.extension.authenticator.RegistrationExtensionAuthenticatorOutput
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString

/**
 * Implements the client side of a passkey with real cryptography.
 */
class FakePasskey(
  val rpId: String,
  val id: String,
  val aaguid: String = RealAuthenticatorDatabase.ApplePasswords,
  private val transports: List<String> = listOf("internal", "hybrid"),
) {
  private val keyPairGenerator = KeyPairGenerator.getInstance("EC").apply {
    initialize(ECGenParameterSpec("secp256r1"), SecureRandom())
  }
  private val keyPair = keyPairGenerator.generateKeyPair()
  private val publicKey = keyPair.public as ECPublicKey

  val coseKey: COSEKey
    get() = EC2COSEKey(
      null,
      COSEAlgorithmIdentifier.ES256,
      null,
      Curve.SECP256R1,
      publicKey.w.affineX.toByteArray(),
      publicKey.w.affineY.toByteArray(),
    )

  val authenticatorDataForRegistration: AuthenticatorData<RegistrationExtensionAuthenticatorOutput>
    get() = AuthenticatorData<RegistrationExtensionAuthenticatorOutput>(
      rpId.encodeUtf8().sha256().toByteArray(),
      93,
      0,
      AttestedCredentialData(
        AAGUID(aaguid),
        id.decodeBase64()!!.toByteArray(),
        coseKey,
      ),
    )

  val authenticatorDataForAuthentication = AuthenticatorData(
    rpId.encodeUtf8().sha256().toByteArray(),
    29,
    0,
    null,
    AuthenticationExtensionsAuthenticatorOutputs.BuilderForAuthentication()
      .build(),
  )

  fun registration(challenge: ByteString, origin: String): PasskeyRegistration {
    val attestationObject = AttestationObject(
      authenticatorDataForRegistration,
      NoneAttestationStatement(),
    )

    val clientData = ClientData(
      type = "webauthn.create",
      challenge = challenge.base64Url(),
      origin = origin,
      crossOrigin = false,
    )
    val clientDataJson = Json.encodeToString(clientData)

    return PasskeyRegistration(
      type = "public-key",
      response = PasskeyRegistration.Response(
        attestationObject = objectConverter.cborConverter.writeValueAsBytes(attestationObject)
          .toByteString(),
        clientDataJSON = clientDataJson.encodeUtf8(),
        publicKeyAlgorithm = -7,
        transports = transports,
      ),
    )
  }

  fun authentication(challenge: ByteString, origin: String): PasskeyAuthentication {
    val clientData = ClientData(
      type = "webauthn.get",
      challenge = challenge.base64Url(),
      origin = origin,
      crossOrigin = false,
    )
    val clientDataJson = Json.encodeToString(clientData).encodeUtf8()

    val authenticatorData = AuthenticatorDataConverter(objectConverter)
      .convert(authenticatorDataForAuthentication)

    val signature = sign(authenticatorData, clientDataJson)

    return PasskeyAuthentication(
      id = id,
      response = PasskeyAuthentication.Response(
        authenticatorData = authenticatorData.toByteString(),
        clientDataJSON = clientDataJson,
        signature = signature,
      ),
    )
  }

  fun sign(authenticatorData: ByteArray, clientDataJSON: ByteString): ByteString {
    val signedData = Buffer()
      .write(authenticatorData)
      .write(clientDataJSON.sha256())

    val signer = Signature.getInstance("SHA256withECDSA").apply {
      initSign(keyPair.private)
    }
    signer.update(signedData.readByteArray())
    return signer.sign().toByteString()
  }

  @Serializable
  private data class ClientData(
    val type: String,
    val challenge: String,
    val origin: String,
    val crossOrigin: Boolean,
  )

  private companion object {
    val objectConverter = ObjectConverter()
  }
}
