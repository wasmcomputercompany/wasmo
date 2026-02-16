package com.wasmo.client.app

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import org.jetbrains.compose.web.css.CSSSizeValue
import org.jetbrains.compose.web.css.CSSUnit
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.height
import org.w3c.dom.CanvasRenderingContext2D

@Composable
fun SegmentedProgressBar(
  childStyle: ChildStyle,
  stepsCompleted: Int,
  stepCount: Int,
  minGap: CSSSizeValue<CSSUnit.px>,
  height: CSSSizeValue<CSSUnit.px>,
) {
  require(stepCount > 0)
  require(stepsCompleted in 0..stepCount)

  Canvas(
    childStyle = object : ChildStyle {
      context(styleScope: StyleScope)
      override fun invoke() {
        childStyle()
        styleScope.height(height)
      }
    },
    renderable = object : Renderable {
      override fun CanvasRenderingContext2D.render(
        now: Double,
        width: Int,
        height: Int,
      ) {
        val width = width.toDouble()
        val height = height.toDouble()
        val gap = minOf(minGap.value * window.devicePixelRatio, width / (stepCount * 2 - 1))

        for (i in 0 until stepCount) {
          fillStyle = when {
            i < stepsCompleted -> "rgb(255 255 255 / 1.0)"
            else -> "rgb(255 255 255 / 0.4)"
          }
          beginPath()
          rect(
            x = (i * (width + gap)) / stepCount,
            y = 0.0,
            w = (i + 1 * (width + gap)) / stepCount - gap,
            h = height,
          )
          fill()
        }
      }
    },
  )
}
