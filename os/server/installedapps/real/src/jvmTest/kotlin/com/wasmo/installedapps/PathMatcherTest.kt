package com.wasmo.installedapps

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.wasmo.packaging.Route
import kotlin.test.Test

class PathMatcherTest {
  @Test
  fun literalDoesntMatch() {
    assertThat(
      PathMatcher().matchOrNull(
        route = Route(
          path = "/images/logo.png",
          resource_path = "/static/pngs/logo.png",
        ),
        urlPath = "/images/logo",
      ),
    ).isNull()

    assertThat(
      PathMatcher().matchOrNull(
        route = Route(
          path = "/images/logo.png",
          objects_key = "/static/pngs/logo.png",
        ),
        urlPath = "/images/logo",
      ),
    ).isNull()
  }

  @Test
  fun literalMatch() {
    assertThat(
      PathMatcher().matchOrNull(
        route = Route(
          path = "/images/logo.png",
          resource_path = "/static/pngs/logo.png",
        ),
        urlPath = "/images/logo.png",
      ),
    ).isEqualTo(PathMatch.Resource("/static/pngs/logo.png"))

    assertThat(
      PathMatcher().matchOrNull(
        route = Route(
          path = "/images/logo.png",
          objects_key = "/static/pngs/logo.png",
        ),
        urlPath = "/images/logo.png",
      ),
    ).isEqualTo(PathMatch.ObjectStore("/static/pngs/logo.png"))
  }

  @Test
  fun wildcardDoesntMatch() {
    assertThat(
      PathMatcher().matchOrNull(
        route = Route(
          path = "/images/**",
          resource_path = "/static/pngs/**",
        ),
        urlPath = "/image/logo.png",
      ),
    ).isNull()

    assertThat(
      PathMatcher().matchOrNull(
        route = Route(
          path = "/images/**",
          objects_key = "/static/pngs/**",
        ),
        urlPath = "/image/logo.png",
      ),
    ).isNull()
  }

  @Test
  fun wildcards() {
    assertThat(
      PathMatcher().matchOrNull(
        route = Route(
          path = "/images/**",
          resource_path = "/static/pngs/**",
        ),
        urlPath = "/images/logo.png",
      ),
    ).isEqualTo(PathMatch.Resource("/static/pngs/logo.png"))

    assertThat(
      PathMatcher().matchOrNull(
        route = Route(
          path = "/images/**",
          objects_key = "/static/pngs/**",
        ),
        urlPath = "/images/logo.png",
      ),
    ).isEqualTo(PathMatch.ObjectStore("/static/pngs/logo.png"))
  }
}
