package com.wasmo.client.app

import androidx.compose.runtime.Composable
import com.wasmo.compose.ComposableElement
import com.wasmo.compose.invoke
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun EnvironmentFrame(
  environment: Environment,
  content: ComposableElement,
) {
  val warningLabel = environment.warningLabel
  if (warningLabel == null) {
    content {}
    return
  }

  Div(
    attrs = {
      style {
        width(100.percent)
        height(100.percent)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
      }
    },
  ) {
    Div(
      attrs = {
        classes("environmentWarningLabel")
        style {
          width(100.percent)
          backgroundColor(rgb(246, 29, 0))
        }
      },
    ) {
      Text(warningLabel)
    }

    content {
      flex(100, 100, 0.px)
    }
  }
}
