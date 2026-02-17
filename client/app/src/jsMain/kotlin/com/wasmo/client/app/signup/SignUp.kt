package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import com.wasmo.compose.SegmentedProgressBar
import com.wasmo.compose.Toolbar
import com.wasmo.compose.ToolbarTitle
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun SignUpToolbar(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
) {
  Toolbar(
    attrs = {
      style {
        marginBottom(8.px)
      }
      attrs()
    },
    title = {
      ToolbarTitle {
        Text("Sign Up")
      }
    },
  )
}

@Composable
fun SignUpSegmentedProgressBar(
  stepsCompleted: Int,
  stepCount: Int,
) {
  SegmentedProgressBar(
    attrs = {
      style {
        marginTop(8.px)
        marginBottom(24.px)
      }
    },
    stepsCompleted = stepsCompleted,
    stepCount = stepCount,
    minGap = 8.px,
    height = 12.px,
  )
}
