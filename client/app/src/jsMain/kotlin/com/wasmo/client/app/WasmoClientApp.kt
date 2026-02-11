package com.wasmo.client.app

import androidx.compose.runtime.Composable
import com.wasmo.common.logging.ConsoleLogger
import com.wasmo.common.logging.Logger
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposableInBody

class WasmoClientApp(
  val logger: Logger = ConsoleLogger,
  val environment: Environment,
) {
  fun start() {
    logger.info("hello")

    renderComposableInBody {
      EnvironmentFrame(environment) { childStyle ->
        Home(childStyle)
      }
    }
  }
}

@Composable
fun Home(childStyle: StyleScope.() -> Unit) {
  Div(
    attrs = {
      style {
        childStyle()
        width(100.percent)
        height(100.percent)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
      }
    },
  ) {
    Img(
      src = "/assets/wasmo1000x300.svg",
      alt = "Wasmo",
      attrs = {
        style {
          property("width", "min(80%, 600px)")
        }
      },
    )

    H1(
      attrs = {
        style {
          margin(20.px, 0.px, 5.px, 0.px)
        }
      },
    ) {
      Text("Your Cloud Computer")
    }

    H2(
      attrs = {
        style {
          margin(5.px, 0.px, 10.px, 0.px)
        }
      },
    ) {
      Text("Coming in 2026")
    }

    Div(
      attrs = {
        style {
          margin(10.px, 0.px, 20.px, 0.px)
        }
      },
    ) {
      A(
        href = "https://github.com/wasmcomputercompany/wasmo",
      ) {
        Text("open source on GitHub")
      }
    }
  }
}
