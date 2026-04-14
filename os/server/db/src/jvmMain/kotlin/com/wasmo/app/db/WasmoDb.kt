package com.wasmo.app.db

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import kotlin.Unit

public interface WasmoDb : Transacter {
  public val accountQueries: AccountQueries

  public val computerQueries: ComputerQueries

  public val computerAccessQueries: ComputerAccessQueries

  public val computerAllocationQueries: ComputerAllocationQueries

  public val computerSpecQueries: ComputerSpecQueries

  public val cookieQueries: CookieQueries

  public val installedAppQueries: InstalledAppQueries

  public val installedAppReleaseQueries: InstalledAppReleaseQueries

  public val inviteQueries: InviteQueries

  public val passkeyQueries: PasskeyQueries

  public val stripeCustomerQueries: StripeCustomerQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = WasmoDb::class.schema

    public operator fun invoke(
      driver: SqlDriver,
      AccountAdapter: Account.Adapter,
      ComputerAccessAdapter: ComputerAccess.Adapter,
      ComputerAdapter: Computer.Adapter,
      ComputerAllocationAdapter: ComputerAllocation.Adapter,
      ComputerSpecAdapter: ComputerSpec.Adapter,
      CookieAdapter: Cookie.Adapter,
      InstalledAppAdapter: InstalledApp.Adapter,
      InstalledAppReleaseAdapter: InstalledAppRelease.Adapter,
      InviteAdapter: Invite.Adapter,
      PasskeyAdapter: Passkey.Adapter,
      StripeCustomerAdapter: StripeCustomer.Adapter,
    ): WasmoDb = WasmoDb::class.newInstance(driver, AccountAdapter, ComputerAccessAdapter, ComputerAdapter, ComputerAllocationAdapter, ComputerSpecAdapter, CookieAdapter, InstalledAppAdapter, InstalledAppReleaseAdapter, InviteAdapter, PasskeyAdapter, StripeCustomerAdapter)
  }
}
