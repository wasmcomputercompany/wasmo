package com.wasmo.client.app.computerlist

import androidx.compose.runtime.Composable
import com.wasmo.client.framework.Ui
import com.wasmo.smartphoneframe.SmartphoneFrame
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Iframe
import org.w3c.dom.HTMLElement

class ComputerListUi : Ui {
  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    SmartphoneFrame(
      attrs = attrs,
    ) { frameAttrs ->
      Iframe(
        attrs = {
          attr("src", "http://jesse99.localhost:8080/")
          style {
            flex("100 100 0")
            border(0.px)
          }
          frameAttrs()
        },
      )
    }
  }
}
