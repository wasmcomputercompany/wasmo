package com.wasmo.client.app.data

import com.wasmo.api.AccountSnapshot
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface AccountDataService {
  val accountSnapshotState: StateFlow<AccountSnapshot>
  val accountSnapshot: AccountSnapshot

  fun receiveAccountSnapshot(snapshot: AccountSnapshot)
}

@Inject
@SingleIn(AppScope::class)
class RealAccountDataService(
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
}
