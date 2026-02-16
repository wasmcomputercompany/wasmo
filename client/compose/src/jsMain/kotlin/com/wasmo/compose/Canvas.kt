package com.wasmo.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.dom.Canvas
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

/**
 * Renders [renderable] when either its contents change, or its size changes.
 */
@Composable
fun Canvas(
  childStyle: ChildStyle,
  renderable: Renderable,
) {
  val canvasState = remember { mutableStateOf<HTMLCanvasElement?>(null) }

  Canvas(
    attrs = {
      style {
        childStyle()
        display(DisplayStyle.Block)
      }
      ref { element ->
        canvasState.value = element
        onDispose {
          canvasState.value = null
        }
      }
    },
  )

  val canvas = canvasState.value ?: return
  var frameTimestamp by remember { mutableStateOf(Double.NaN) }

  fun requestFrame() {
    window.requestAnimationFrame { timestamp ->
      frameTimestamp = timestamp
    }
  }

  // Re-render when resized.
  DisposableEffect(canvas) {
    val resizeObserver = ResizeObserver {
      requestFrame()
    }

    resizeObserver.observe(canvas)
    onDispose {
      resizeObserver.unobserve(canvas)
    }
  }

  key(frameTimestamp) {
    if (frameTimestamp.isNaN()) return

    val boundingClientRect = canvas.getBoundingClientRect()
    val devicePixelRatio = window.devicePixelRatio
    val width = (boundingClientRect.width * devicePixelRatio).toInt()
    val height = (boundingClientRect.height * devicePixelRatio).toInt()

    canvas.width = width
    canvas.height = height
    val context = canvas.getContext("2d") as CanvasRenderingContext2D
    context.save()
    renderable(context, width, height)
    context.restore()
  }
}

fun interface Renderable {
  @Composable
  operator fun invoke(
    context: CanvasRenderingContext2D,
    width: Int,
    height: Int,
  )
}
