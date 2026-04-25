package com.wasmo.ktor

import com.wasmo.accounts.AccountSnapshotAction
import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Challenger
import com.wasmo.accounts.Client
import com.wasmo.accounts.SignOutAction
import com.wasmo.accounts.invite.CreateInviteAction
import com.wasmo.accounts.passkeys.AuthenticatePasskeyAction
import com.wasmo.accounts.passkeys.RegisterPasskeyAction
import com.wasmo.calls.CallDataService
import com.wasmo.calls.RealCallDataService
import com.wasmo.computers.AfterCheckoutAction
import com.wasmo.computers.CreateComputerSpecAction
import com.wasmo.emails.ConfirmEmailAddressAction
import com.wasmo.emails.LinkEmailAddressAction
import com.wasmo.installedapps.CallAppAction
import com.wasmo.installedapps.InstallAppAction
import com.wasmo.passkeys.PasskeyChecker
import com.wasmo.passkeys.RealPasskeyChecker
import com.wasmo.website.OsPageAction
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@GraphExtension(
  scope = CallScope::class,
)
interface CallGraph {
  val accountSnapshotAction: AccountSnapshotAction
  val afterCheckoutAction: AfterCheckoutAction
  val authenticatePasskeyAction: AuthenticatePasskeyAction
  val callAppAction: CallAppAction
  val confirmEmailAddressAction: ConfirmEmailAddressAction
  val createComputerSpecAction: CreateComputerSpecAction
  val createInviteAction: CreateInviteAction
  val installAppAction: InstallAppAction
  val linkEmailAddressAction: LinkEmailAddressAction
  val osPageAction: OsPageAction
  val registerPasskeyAction: RegisterPasskeyAction
  val signOutAction: SignOutAction

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
