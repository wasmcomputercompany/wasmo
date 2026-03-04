package com.wasmo.ktor

import com.wasmo.accounts.AccountSnapshotAction
import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Challenger
import com.wasmo.accounts.Client
import com.wasmo.accounts.ConfirmEmailAddressAction
import com.wasmo.accounts.LinkEmailAddressAction
import com.wasmo.accounts.invite.CreateInviteAction
import com.wasmo.accounts.passkeys.AuthenticatePasskeyAction
import com.wasmo.accounts.passkeys.RegisterPasskeyAction
import com.wasmo.calls.CallDataService
import com.wasmo.calls.RealCallDataService
import com.wasmo.computers.AfterCheckoutAction
import com.wasmo.computers.CreateComputerSpecAction
import com.wasmo.computers.InstallAppAction
import com.wasmo.passkeys.PasskeyChecker
import com.wasmo.passkeys.RealPasskeyChecker
import com.wasmo.website.HostPageAction
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@GraphExtension(
  scope = CallScope::class,
)
interface CallGraph {
  val accountSnapshotAction: AccountSnapshotAction
  val createInviteAction: CreateInviteAction
  val registerPasskeyAction: RegisterPasskeyAction
  val authenticatePasskeyAction: AuthenticatePasskeyAction
  val linkEmailAddressAction: LinkEmailAddressAction
  val confirmEmailAddressAction: ConfirmEmailAddressAction
  val createComputerSpecAction: CreateComputerSpecAction
  val installAppAction: InstallAppAction
  val hostPageAction: HostPageAction
  val afterCheckoutAction: AfterCheckoutAction

  @Provides
  @SingleIn(CallScope::class)
  fun provideChallenger(
    client: Client,
  ): Challenger = client.challenger

  @Binds
  fun bindPasskeyChecker(
    real: RealPasskeyChecker,
  ): PasskeyChecker

  @Binds
  fun bindCallDataService(
    real: RealCallDataService,
  ): CallDataService

  @GraphExtension.Factory
  interface Factory {
    fun create(@Provides client: Client): CallGraph
  }
}
