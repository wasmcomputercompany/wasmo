package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import com.wasmo.compose.ChildStyle
import com.wasmo.compose.SegmentedProgressBar
import com.wasmo.compose.Toolbar
import com.wasmo.compose.ToolbarTitle
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Text

@Composable
fun SignUpToolbar(
  childStyle: ChildStyle,
) {
  Toolbar(
    childStyle = ChildStyle {
      childStyle()
      marginBottom(8.px)
    },
    title = {
      ToolbarTitle(
        childStyle = ChildStyle {
        },
      ) {
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
    childStyle = ChildStyle {
      marginTop(8.px)
      marginBottom(24.px)
    },
    stepsCompleted = stepsCompleted,
    stepCount = stepCount,
    minGap = 8.px,
    height = 12.px,
  )
}
