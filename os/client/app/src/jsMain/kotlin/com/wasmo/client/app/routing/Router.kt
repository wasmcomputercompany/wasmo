package com.wasmo.client.app.routing

import androidx.compose.runtime.mutableStateOf
import com.wasmo.api.routes.Route
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.SignOutRoute
import com.wasmo.client.app.browser.Browser
import com.wasmo.client.identifiers.ClientAppScope
import com.wasmo.framework.Url
import com.wasmo.framework.decodeUrl
import com.wasmo.framework.encode
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope

/**
 * Unify application-triggered navigation and browser-triggered navigation.
 */
@Inject
@SingleIn(ClientAppScope::class)
class Router(
  val scope: CoroutineScope,
  val routeCodec: RouteCodec,
  val browser: Browser,
) {
  private val backStack = mutableListOf<Url>()
  val current = mutableStateOf<Route?>(null)

  fun start() {
    browser.onpopstate = {
      goToRoute(routeCodec.decode(browser.locationHref.decodeUrl()))
    }

    goToRoute(routeCodec.decode(browser.locationHref.decodeUrl()))
  }

  fun goTo(
    route: Route,
    transitionDirection: TransitionDirection,
  ) = goToRoute(route, transitionDirection)

  /**
   * @param transitionDirection null if the browser's forwards and backwards buttons triggered this
   *     navigation.
   */
  private fun goToRoute(
    nextRoute: Route,
    transitionDirection: TransitionDirection? = null,
  ) {
    val nextUrl = routeCodec.encode(nextRoute)
    var currentUrl: Url? = null
    var backUrl: Url? = null

    if (backStack.isNotEmpty()) {
      currentUrl = backStack.last()
      if (backStack.size > 1) {
        backUrl = backStack[backStack.size - 2]
      }
    }

    if (nextUrl == currentUrl) return

    // Switching subdomains triggers a browser navigation & fresh page load.
    if (requiresBrowserNavigation(currentUrl, nextUrl, nextRoute)) {
      browser.locationHref = nextUrl.encode()
      return
    }

    // Infer the animation based on whether this page is 'back' in history.
    val displayTransition = transitionDirection
      ?: when {
        nextUrl == backUrl -> TransitionDirection.POP
        else -> TransitionDirection.PUSH
      }

    when (displayTransition) {
      TransitionDirection.POP if nextUrl == backUrl -> {
        // Pop the current page off the back stack.
        backStack.removeLast()
        if (transitionDirection != null) browser.back()
      }

      TransitionDirection.PUSH -> {
        // Push a new page onto the back stack.
        backStack.add(nextUrl)
        if (transitionDirection != null) browser.pushState(Unit, "", nextUrl)
      }

      else -> {
        // Pop the current page off the back stack, then push a new page.
        backStack.removeLastOrNull()
        backStack.add(nextUrl)
        if (transitionDirection != null) browser.replaceState(Unit, "", nextUrl)
      }
    }

    // TODO: show transition?
    current.value = nextRoute
  }

  /** Returns true to trigger a page load on this navigation. */
  private fun requiresBrowserNavigation(currentUrl: Url?, nextUrl: Url, nextRoute: Route): Boolean {
    // When the subdomain changes, get the browser to do the navigation.
    if (currentUrl != null && nextUrl.subdomain != currentUrl.subdomain) {
      return true
    }

    return when (nextRoute) {
      SignOutRoute -> true
      else -> false
    }
  }
}

