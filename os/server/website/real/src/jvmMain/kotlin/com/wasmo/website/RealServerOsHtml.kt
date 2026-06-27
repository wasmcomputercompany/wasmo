package com.wasmo.website

import com.wasmo.api.AccountSnapshot
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.InviteTicket
import com.wasmo.api.payments.ComputerPaymentMethod
import com.wasmo.api.SignInSnapshot
import com.wasmo.api.WasmoJson
import com.wasmo.api.routes.RoutingContext
import com.wasmo.framework.ContentTypes
import com.wasmo.framework.MapPageData
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.framework.write
import com.wasmo.identifiers.Deployment
import com.wasmo.support.okiohtml.writeHtml
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.title
import kotlinx.html.unsafe
import okio.BufferedSink

@AssistedInject
class RealServerOsHtml(
  override val deployment: Deployment,
  override val computerPaymentMethod: ComputerPaymentMethod,
  @Assisted override val accountSnapshot: AccountSnapshot,
  @Assisted override val routingContext: RoutingContext,
  @Assisted override val inviteTicket: InviteTicket?,
  @Assisted override val computerSnapshot: ComputerSnapshot?,
  @Assisted override val computerListSnapshot: ComputerListSnapshot?,
  @Assisted override val signInSnapshot: SignInSnapshot?,
) : ServerOsHtml, ResponseBody {
  val pageData: MapPageData
    get() = MapPageData.Builder(WasmoJson)
      .put("computer_payment_method", computerPaymentMethod)
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
        if (signInSnapshot != null) {
          put("sign_in_snapshot", signInSnapshot)
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
        meta(name = "color-scheme", content = "light dark")
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
          href = "https://fonts.googleapis.com/css2?family=Figtree:ital,wght@0,300..900;1,300..900&display=swap",
        )
        link(rel = "icon", href = "/favicon.ico") {
          attributes["sizes"] = "32x32"
        }
        link(rel = "icon", href = "/icon.svg", type = "image/svg+xml")
        link(rel = "apple-touch-icon", href = "/apple-touch-icon.png")
        link(rel = "stylesheet", href = "/assets/pico-for-wasmo.css")
        link(rel = "stylesheet", href = "/assets/Wasmo.css")

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

  @AssistedFactory
  interface Factory : ServerOsHtml.Factory {
    override fun create(
      routingContext: RoutingContext,
      accountSnapshot: AccountSnapshot,
      inviteTicket: InviteTicket?,
      computerSnapshot: ComputerSnapshot?,
      computerListSnapshot: ComputerListSnapshot?,
      signInSnapshot: SignInSnapshot?,
    ): RealServerOsHtml
  }
}
