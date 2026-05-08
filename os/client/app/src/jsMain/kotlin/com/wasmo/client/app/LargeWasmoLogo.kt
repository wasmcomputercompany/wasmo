package com.wasmo.client.app

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.AlignSelf
import org.jetbrains.compose.web.css.alignSelf
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Img
import org.w3c.dom.HTMLImageElement

@Composable
fun LargeWasmoLogo(
  attrs: AttrsScope<HTMLImageElement>.() -> Unit = {},
) {
  Img(
    src = "/assets/wasmo1000x300.svg",
    alt = "Wasmo",
    attrs = {
      style {
        alignSelf(AlignSelf.Center)
        width(350.px)
        height(105.px)
      }
      attrs()
    },
  )
}
