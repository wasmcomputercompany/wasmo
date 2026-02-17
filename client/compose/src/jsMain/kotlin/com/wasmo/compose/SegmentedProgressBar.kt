package com.wasmo.compose

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.CSSSizeValue
import org.jetbrains.compose.web.css.CSSUnit
import org.jetbrains.compose.web.css.height
import org.w3c.dom.HTMLCanvasElement

@Composable
fun SegmentedProgressBar(
  attrs: AttrsScope<HTMLCanvasElement>.() -> Unit = {},
  stepsCompleted: Int,
  stepCount: Int,
  minGap: CSSSizeValue<CSSUnit.px>,
  height: CSSSizeValue<CSSUnit.px>,
) {
  require(stepCount > 0)
  require(stepsCompleted in 0..stepCount)

  Canvas(
    attrs = {
      style {
        height(height)
      }
      attrs()
    },
  ) { context, width, height ->
    val width = width.toDouble()
    val height = height.toDouble()
    val gap = minOf(minGap.value * window.devicePixelRatio, width / (stepCount * 2 - 1))

    for (i in 0 until stepCount) {
      context.fillStyle = when {
        i < stepsCompleted -> "rgb(255 255 255 / 1.0)"
        else -> "rgb(255 255 255 / 0.4)"
      }
      context.beginPath()
      context.rect(
        x = (i * (width + gap)) / stepCount,
        y = 0.0,
        w = (i + 1 * (width + gap)) / stepCount - gap,
        h = height,
      )
      context.fill()
    }
  }
}
