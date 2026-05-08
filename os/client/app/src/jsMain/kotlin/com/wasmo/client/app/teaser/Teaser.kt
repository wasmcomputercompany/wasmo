package com.wasmo.client.app.teaser

import androidx.compose.runtime.Composable
import com.wasmo.compose.Column
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun Teaser(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
) {
  Column(
    attrs = attrs,
    alignItems = AlignItems.Center,
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
