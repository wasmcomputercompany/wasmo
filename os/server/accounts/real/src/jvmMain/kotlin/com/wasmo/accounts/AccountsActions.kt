package com.wasmo.accounts

import com.wasmo.accounts.invite.CreateInviteRpc
import com.wasmo.framework.UserAgent

interface AccountsActions {
  val accountSnapshotRpc: AccountSnapshotRpc
  val createInviteRpc: CreateInviteRpc
  val signOutRpc: SignOutRpc
  val signOutPage: SignOutPage

  interface Factory {
    fun create(userAgent: UserAgent): AccountsActions
  }
}
