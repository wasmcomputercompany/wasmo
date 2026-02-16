package com.wasmo.client.app

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Canvas
import org.w3c.dom.CanvasRenderingContext2D

/**
 * Renders [renderable] when its contents change, and when this element's size changes.
 */
@Composable
fun Canvas(
  childStyle: ChildStyle,
  renderable: Renderable,
) {
  Canvas(
    attrs = {
      style {
        childStyle()
      }
      ref { element ->
        fun render(now: Double) {
          val boundingClientRect = element.getBoundingClientRect()
          val devicePixelRatio = window.devicePixelRatio
          val width = (boundingClientRect.width * devicePixelRatio).toInt()
          val height = (boundingClientRect.height * devicePixelRatio).toInt()

          with(renderable) {
            element.width = width
            element.height = height
            val context = element.getContext("2d") as CanvasRenderingContext2D
            context.save()
            try {
              context.render(now, width, height)
            } finally {
              context.restore()
            }
          }
        }

        fun requestFrame() {
          window.requestAnimationFrame { timestamp ->
            render(timestamp)
          }
        }

        requestFrame()

        val resizeObserver = ResizeObserver {
          requestFrame()
        }

        resizeObserver.observe(element)
        onDispose {
          resizeObserver.unobserve(element)
        }
      }
    },
  )
}

interface Renderable {
  fun CanvasRenderingContext2D.render(now: Double, width: Int, height: Int)
}
