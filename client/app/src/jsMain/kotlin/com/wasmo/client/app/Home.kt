package com.wasmo.client.app

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
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
import org.w3c.dom.HTMLDivElement

@Composable
fun Home(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  showSignUp: Boolean,
  eventListener: (HomeEvent) -> Unit,
) {
  Div(
    attrs = {
      classes("HomeScreen")
      style {
        width(100.percent)
        height(100.percent)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
      }
      attrs()
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

    if (showSignUp) {
      Div(
        attrs = {
          style {
            margin(10.px, 0.px, 10.px, 0.px)
          }
        },
      ) {
        A(
          attrs = {
            onClick {
              eventListener(HomeEvent.SignUp)
            }
          },
        ) {
          Text("Sign Up")
        }
      }
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

interface HomeEvent {
  object SignUp : HomeEvent
}
