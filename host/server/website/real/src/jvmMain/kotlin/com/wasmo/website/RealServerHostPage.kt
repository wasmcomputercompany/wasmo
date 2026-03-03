package com.wasmo.website

import com.wasmo.api.AccountSnapshot
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.InviteTicket
import com.wasmo.api.WasmoJson
import com.wasmo.api.routes.RoutingContext
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.deployment.Deployment
import com.wasmo.framework.ContentTypes
import com.wasmo.framework.MapPageData
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.framework.write
import com.wasmo.framework.writeHtml
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.title
import kotlinx.html.unsafe
import okio.BufferedSink

class RealServerHostPage(
  override val deployment: Deployment,
  override val stripePublishableKey: StripePublishableKey,
  override val accountSnapshot: AccountSnapshot,
  override val routingContext: RoutingContext,
  override val inviteTicket: InviteTicket?,
  override val computerSnapshot: ComputerSnapshot?,
  override val computerListSnapshot: ComputerListSnapshot?,
) : ServerHostPage, ResponseBody {
  val pageData: MapPageData
    get() = MapPageData.Builder(WasmoJson)
      .put("stripe_publishable_key", stripePublishableKey)
      .put("routing_context", routingContext)
      .put("account_snapshot", accountSnapshot)
      .apply {
        if (inviteTicket != null) {
          put("invite_ticket", inviteTicket)
        }
        if (computerSnapshot != null) {
          put("computer_snapshot", computerSnapshot)
        }
        if (computerListSnapshot != null) {
          put("computer_list_snapshot", computerListSnapshot)
        }
      }
      .build()

  override val response: Response<ResponseBody>
    get() = Response(
      contentType = ContentTypes.TextHtml,
      body = this,
    )

  override fun write(sink: BufferedSink) {
    sink.writeUtf8("<!DOCTYPE html>")
    sink.writeHtml {
      head {
        meta(charset = "utf-8")
        title("Wasmo")
        meta(
          name = "viewport",
          content = "width=device-width, initial-scale=1, minimum-scale=1, maximum-scale=1",
        )
        meta(
          name = "theme-color",
          content = "#ffffff",
        )
        meta {
          attributes["property"] = "og:image"
          attributes["content"] = deployment.baseUrl.resolve("/assets/og-image.png").toString()
        }
        meta {
          attributes["property"] = "og:image:width"
          attributes["content"] = "1200"
        }
        meta {
          attributes["property"] = "og:image:height"
          attributes["content"] = "630"
        }

        link(rel = "preconnect", href = "https://fonts.gstatic.com") {
          attributes["crossorigin"] = "anonymous"
        }
        link(
          rel = "stylesheet",
          href = "https://fonts.googleapis.com/css2?family=Outfit:wght@100..900&display=swap",
        )
        link(rel = "icon", href = "/favicon.ico") {
          attributes["sizes"] = "32x32"
        }
        link(rel = "icon", href = "/icon.svg", type = "image/svg+xml")
        link(rel = "apple-touch-icon", href = "/apple-touch-icon.png")
        link(rel = "stylesheet", href = "/assets/Wasmo.css")
        link(rel = "stylesheet", href = "/assets/launcher/Launcher.css")

        script(src = "/assets/wasmo.js") {}
        script(src = "https://js.stripe.com/clover/stripe.js") {}

        pageData.write(this)

        script {
          unsafe {
            raw("""wasmo.startOnLoad();""")
          }
        }
      }
      body {
      }
    }
  }

  class Factory(
    private val deployment: Deployment,
    private val stripePublishableKey: StripePublishableKey,
  ) : ServerHostPage.Factory {
    override fun create(
      routingContext: RoutingContext,
      accountSnapshot: AccountSnapshot,
      inviteTicket: InviteTicket?,
      computerSnapshot: ComputerSnapshot?,
      computerListSnapshot: ComputerListSnapshot?,
    ) = RealServerHostPage(
      deployment = deployment,
      stripePublishableKey = stripePublishableKey,
      accountSnapshot = accountSnapshot,
      routingContext = routingContext,
      inviteTicket = inviteTicket,
      computerSnapshot = computerSnapshot,
      computerListSnapshot = computerListSnapshot,
    )
  }
}
