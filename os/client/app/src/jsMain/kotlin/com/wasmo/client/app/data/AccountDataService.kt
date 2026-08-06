package com.wasmo.client.app.data

import com.wasmo.api.AccountSnapshot
import com.wasmo.api.ConfirmEmailAddressRequest
import com.wasmo.api.ConfirmEmailAddressResponse
import com.wasmo.api.CreateUsernameRequest
import com.wasmo.api.CreateUsernameResponse
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.LinkUsernameRequest
import com.wasmo.api.LinkUsernameResponse
import com.wasmo.api.WasmoApi
import com.wasmo.client.identifiers.ClientAppScope
import com.wasmo.identifiers.Secret
import com.wasmo.identifiers.UsernameSlug
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface AccountDataService {
  val accountSnapshotFlow: StateFlow<AccountSnapshot>
  val accountSnapshot: AccountSnapshot

  /** Call this when a new account snapshot is received. */
  fun receiveAccountSnapshot(snapshot: AccountSnapshot)

  /** Returns a challenge token. */
  suspend fun linkEmailAddress(
    unverifiedEmailAddress: String,
  ): String

  suspend fun confirmEmailAddress(
    unverifiedEmailAddress: String,
    challengeToken: String,
    challengeCode: String,
  ): ConfirmEmailAddressResponse

  suspend fun createUsername(
    username: UsernameSlug,
    // "" means no password
    password: String = "",
  ): CreateUsernameResponse

  suspend fun linkUsername(
    username: UsernameSlug,
    // "" means no password
    password: String = "",
  ): LinkUsernameResponse
}

@Inject
@SingleIn(ClientAppScope::class)
class RealAccountDataService(
  private val wasmoApi: WasmoApi,
  accountSnapshot: AccountSnapshot,
) : AccountDataService {
  private val accountSnapshot_ = MutableStateFlow(accountSnapshot)

  override val accountSnapshotFlow: StateFlow<AccountSnapshot>
    get() = accountSnapshot_

  override val accountSnapshot: AccountSnapshot
    get() = accountSnapshotFlow.value

  override fun receiveAccountSnapshot(snapshot: AccountSnapshot) {
    accountSnapshot_.value = snapshot
  }

  override suspend fun linkEmailAddress(unverifiedEmailAddress: String): String {
    val response = wasmoApi.linkEmailAddress(
      LinkEmailAddressRequest(unverifiedEmailAddress),
    )
    return response.challengeToken
  }

  override suspend fun confirmEmailAddress(
    unverifiedEmailAddress: String,
    challengeToken: String,
    challengeCode: String,
  ) = wasmoApi.confirmEmailAddress(
    ConfirmEmailAddressRequest(
      unverifiedEmailAddress = unverifiedEmailAddress,
      challengeToken = challengeToken,
      challengeCode = challengeCode,
    ),
  )

  override suspend fun createUsername(
    username: UsernameSlug,
    password: String,
  ) = wasmoApi.createUsername(
    CreateUsernameRequest(
      username = username,
      password = password,
    ),
  )

  override suspend fun linkUsername(
    username: UsernameSlug,
    password: String,
  ) = wasmoApi.linkUsername(
    LinkUsernameRequest(
      username = username,
      password = password,
    ),
  )
}
