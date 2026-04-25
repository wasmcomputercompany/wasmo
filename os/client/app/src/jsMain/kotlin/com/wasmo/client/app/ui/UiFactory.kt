package com.wasmo.client.app.ui

import com.wasmo.api.InviteTicket
import com.wasmo.api.routes.AdminRoute
import com.wasmo.api.routes.AfterCheckoutRoute
import com.wasmo.api.routes.AppRoute
import com.wasmo.api.routes.BuildYoursRoute
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.HomeRoute
import com.wasmo.api.routes.InviteRoute
import com.wasmo.api.routes.NotFoundRoute
import com.wasmo.api.routes.Route
import com.wasmo.api.routes.SignUpRoute
import com.wasmo.client.app.buildyours.BuildYoursUi
import com.wasmo.client.app.computer.ComputerUi
import com.wasmo.client.app.home.HomeUi
import com.wasmo.client.app.invite.InviteUi
import com.wasmo.client.app.signup.SignUpPresenter
import com.wasmo.client.app.signup.SignUpUi
import com.wasmo.client.framework.Ui
import com.wasmo.client.identifiers.ClientAppScope
import com.wasmo.framework.PageData
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(ClientAppScope::class)
class UiFactory(
  private val pageData: PageData,
  private val inviteUiFactory: InviteUi.Factory,
  private val buildYoursUiFactory: BuildYoursUi.Factory,
  private val homeUiFactory: HomeUi.Factory,
  private val computerUiFactory: ComputerUi.Factory,
  private val signUpPresenterFactory: SignUpPresenter.Factory,
  private val signUpUiFactory: SignUpUi.Factory,
) {
  fun create(route: Route): Ui {
    return when (route) {
      AdminRoute -> UnimplementedUi("Admin")

      is AfterCheckoutRoute -> UnimplementedUi("After Checkout")

      BuildYoursRoute -> buildYoursUiFactory.create()

      is AppRoute -> UnimplementedUi("App ${route.appSlug.value} on ${route.computerSlug.value}")

      is ComputerHomeRoute -> computerUiFactory.create(route.slug)

      is InviteRoute -> inviteUiFactory.create(
        inviteTicket = pageData.get<InviteTicket>("invite_ticket")
          ?: error("required invite_ticket pageData not found"),
      )

      HomeRoute -> homeUiFactory.create()

      SignUpRoute -> {
        val presenter = signUpPresenterFactory.create()
        signUpUiFactory.create(presenter)
      }

      NotFoundRoute -> UnimplementedUi("Not Found")
    }
  }
}
