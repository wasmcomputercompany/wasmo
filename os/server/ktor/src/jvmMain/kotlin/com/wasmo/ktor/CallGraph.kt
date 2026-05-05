package com.wasmo.ktor

import com.wasmo.accounts.AccountSnapshotRpc
import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Challenger
import com.wasmo.accounts.Client
import com.wasmo.accounts.SignOutPage
import com.wasmo.accounts.SignOutRpc
import com.wasmo.accounts.invite.CreateInviteRpc
import com.wasmo.accounts.passkeys.AuthenticatePasskeyRpc
import com.wasmo.accounts.passkeys.PasskeyActions
import com.wasmo.accounts.passkeys.RegisterPasskeyRpc
import com.wasmo.calls.CallDataService
import com.wasmo.calls.RealCallDataService
import com.wasmo.computers.AfterCheckoutPage
import com.wasmo.computers.ComputersActions
import com.wasmo.computers.CreateComputerSpecRpc
import com.wasmo.emails.ConfirmEmailAddressRpc
import com.wasmo.emails.LinkEmailAddressRpc
import com.wasmo.installedapps.CallAppAction
import com.wasmo.installedapps.ComputerActions
import com.wasmo.installedapps.InstallAppRpc
import com.wasmo.installedapps.InstalledAppActions
import com.wasmo.passkeys.PasskeyChecker
import com.wasmo.passkeys.RealPasskeyChecker
import com.wasmo.website.OsPage
import com.wasmo.website.WebsiteActions
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@GraphExtension(
  scope = CallScope::class,
)
interface CallGraph :
  ComputerActions,
  ComputersActions,
  InstalledAppActions,
  PasskeyActions,
  WebsiteActions {
  val accountSnapshotRpc: AccountSnapshotRpc
  val afterCheckoutPage: AfterCheckoutPage
  override val authenticatePasskeyRpc: AuthenticatePasskeyRpc
  override val callAppAction: CallAppAction
  val confirmEmailAddressRpc: ConfirmEmailAddressRpc
  override val createComputerSpecRpc: CreateComputerSpecRpc
  val createInviteRpc: CreateInviteRpc
  override val installAppRpc: InstallAppRpc
  val linkEmailAddressRpc: LinkEmailAddressRpc
  override val osPage: OsPage
  override val registerPasskeyRpc: RegisterPasskeyRpc
  val signOutRpc: SignOutRpc
  val signOutPage: SignOutPage

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
