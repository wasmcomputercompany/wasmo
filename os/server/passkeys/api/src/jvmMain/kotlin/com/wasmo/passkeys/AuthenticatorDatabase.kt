package com.wasmo.passkeys

/**
 * https://fidoalliance.org/metadata/
 * https://passkeydeveloper.github.io/passkey-authenticator-aaguids/explorer/
 */
interface AuthenticatorDatabase {
  fun forAaguid(aaguid: String): String?
}
