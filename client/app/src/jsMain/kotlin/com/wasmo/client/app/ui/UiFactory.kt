package com.wasmo.client.app.ui

import com.wasmo.api.AccountSnapshot
import com.wasmo.api.InviteTicket
import com.wasmo.client.app.buildyours.BuildYoursUi
import com.wasmo.client.app.invite.InviteUi
import com.wasmo.client.app.teaser.TeaserUi
import com.wasmo.client.framework.Ui
import com.wasmo.common.routes.AdminRoute
import com.wasmo.common.routes.AfterCheckoutRoute
import com.wasmo.common.routes.BuildYoursRoute
import com.wasmo.common.routes.ComputerHomeRoute
import com.wasmo.common.routes.ComputersRoute
import com.wasmo.common.routes.InviteRoute
import com.wasmo.common.routes.NotFoundRoute
import com.wasmo.common.routes.Route
import com.wasmo.common.routes.TeaserRoute
import com.wasmo.framework.PageData

class UiFactory(
  private val pageData: PageData,
  private val accountSnapshot: AccountSnapshot,
  private val inviteUiFactory: InviteUi.Factory,
  private val buildYoursUiFactory: BuildYoursUi.Factory,
  private val teaserUiFactory: TeaserUi.Factory,
) {
  fun create(route: Route): Ui {
    return when (route) {
      AdminRoute -> UnimplementedUi("Admin")

      is AfterCheckoutRoute -> UnimplementedUi("After Checkout")

      BuildYoursRoute -> buildYoursUiFactory.create()

      is ComputerHomeRoute -> UnimplementedUi("Computer Home")

      ComputersRoute -> UnimplementedUi("Computers")

      is InviteRoute -> inviteUiFactory.create(
        accountSnapshot = accountSnapshot,
        inviteTicket = pageData.get<InviteTicket>("invite_ticket")
          ?: error("required invite_ticket pageData not found"),
      )

      NotFoundRoute -> UnimplementedUi("Not Found")

      TeaserRoute -> teaserUiFactory.create()
    }
  }
}

