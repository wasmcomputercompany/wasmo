package com.wasmo.client.app.data

import com.wasmo.api.AccountSnapshot
import com.wasmo.api.ConfirmEmailAddressRequest
import com.wasmo.api.ConfirmEmailAddressResponse
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.WasmoApi
import com.wasmo.client.identifiers.ClientAppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface AccountDataService {
  val accountSnapshotState: StateFlow<AccountSnapshot>
  val accountSnapshot: AccountSnapshot

  /** Call this when a new account snapshot is received. */
  fun receiveAccountSnapshot(snapshot: AccountSnapshot)

  suspend fun linkEmailAddress(
    unverifiedEmailAddress: String,
  ): Boolean

  suspend fun confirmEmailAddress(
    unverifiedEmailAddress: String,
    challengeCode: String,
  ): ConfirmEmailAddressResponse
}

@Inject
@SingleIn(ClientAppScope::class)
class RealAccountDataService(
  private val wasmoApi: WasmoApi,
  accountSnapshot: AccountSnapshot,
) : AccountDataService {
  private val accountSnapshot_ = MutableStateFlow(accountSnapshot)

  override val accountSnapshotState: StateFlow<AccountSnapshot>
    get() = accountSnapshot_

  override val accountSnapshot: AccountSnapshot
    get() = accountSnapshotState.value

  override fun receiveAccountSnapshot(snapshot: AccountSnapshot) {
    accountSnapshot_.value = snapshot
  }

  override suspend fun linkEmailAddress(unverifiedEmailAddress: String): Boolean {
    val response = wasmoApi.linkEmailAddress(
      LinkEmailAddressRequest(unverifiedEmailAddress),
    )
    return response.challengeSent
  }

  override suspend fun confirmEmailAddress(
    unverifiedEmailAddress: String,
    challengeCode: String,
  ): ConfirmEmailAddressResponse {
    return wasmoApi.confirmEmailAddress(
      ConfirmEmailAddressRequest(
        unverifiedEmailAddress = unverifiedEmailAddress,
        challengeCode = challengeCode,
      ),
    )
  }
}
