package com.wasmo.journal.app.util

import androidx.compose.runtime.mutableStateOf
import kotlinx.browser.window
import org.w3c.dom.url.URL

/**
 * A minimal typesafe router for a single page application.
 */
class Router<R>(
  val routeCodec: RouteCodec<R>,
) {
  private val backStack = mutableListOf<String>()
  val current = mutableStateOf<R?>(null)

  fun start() {
    window.onpopstate = {
      goToInternal(routeCodec.decode(URL(window.location.href).pathname))
    }

    goToInternal(routeCodec.decode(URL(window.location.href).pathname))
  }

  fun goTo(
    route: R,
    direction: Direction,
  ) = goToInternal(route, direction)

  /**
   * @param direction null if the browser's forwards and backwards buttons triggered this
   *     navigation.
   */
  private fun goToInternal(
    nextRoute: R,
    direction: Direction? = null,
  ) {
    val nextUrl = routeCodec.encode(nextRoute)
    var currentUrl: String? = null
    var backUrl: String? = null

    if (backStack.isNotEmpty()) {
      currentUrl = backStack.last()
      if (backStack.size > 1) {
        backUrl = backStack[backStack.size - 2]
      }
    }

    if (nextUrl == currentUrl) return

    // Infer the animation based on whether this page is 'back' in history.
    val displayTransition = direction
      ?: when {
        nextUrl == backUrl -> Direction.Pop
        else -> Direction.Push
      }

    when (displayTransition) {
      Direction.Pop if nextUrl == backUrl -> {
        // Pop the current page off the back stack.
        backStack.removeLast()
        if (direction != null) {
          window.history.back()
        }
      }

      Direction.Push -> {
        // Push a new page onto the back stack.
        backStack.add(nextUrl)
        if (direction != null) {
          window.history.pushState(Unit, "", nextUrl)
        }
      }

      else -> {
        // Pop the current page off the back stack, then push a new page.
        backStack.removeLastOrNull()
        backStack.add(nextUrl)
        if (direction != null) {
          window.history.replaceState(Unit, "", nextUrl)
        }
      }
    }

    // TODO: show transition?
    current.value = nextRoute
  }

  enum class Direction {
    Push,
    Replace,
    Pop,
  }

  interface RouteCodec<R> {
    fun encode(route: R): String
    fun decode(path: String): R
  }
}
