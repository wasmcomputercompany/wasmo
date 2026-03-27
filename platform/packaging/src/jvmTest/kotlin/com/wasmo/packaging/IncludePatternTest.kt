package com.wasmo.packaging

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class IncludePatternTest {
  @Test
  fun expectedMatches() {
    val includePattern = IncludePattern("**/*.mp3")
    assertThat(includePattern.matches(".mp3")).isTrue()
    assertThat(includePattern.matches("abc.mp3")).isTrue()
    assertThat(includePattern.matches("/abc.mp3")).isTrue()
    assertThat(includePattern.matches("abc/def.mp3")).isTrue()
    assertThat(includePattern.matches("/abc/def.mp3")).isTrue()
    assertThat(includePattern.matches("abc/def/ghi.mp3")).isTrue()
    assertThat(includePattern.matches("/abc/def/ghi.mp3")).isTrue()

    assertThat(includePattern.matches("a.mp3")).isTrue()
    assertThat(includePattern.matches("/a.mp3")).isTrue()
    assertThat(includePattern.matches("a/b.mp3")).isTrue()
    assertThat(includePattern.matches("/a/b.mp3")).isTrue()
    assertThat(includePattern.matches("a/b/c.mp3")).isTrue()
    assertThat(includePattern.matches("/a/b/c.mp3")).isTrue()

    assertThat(includePattern.matches("/.mp3")).isTrue()
    assertThat(includePattern.matches("//.mp3")).isTrue()
  }

  @Test
  fun leadingSlash() {
    val includePattern = IncludePattern("*.mp3")
    assertThat(includePattern.matches("/abc.mp3")).isTrue()
  }

  @Test
  fun matchingIsCaseInsensitive() {
    val includePattern = IncludePattern("**/*.mp3")
    assertThat(includePattern.matches("abc.MP3")).isTrue()
    assertThat(includePattern.matches("abc.Mp3")).isTrue()
  }

  @Test
  fun expectedNonMatches() {
    val includePattern = IncludePattern("**/*.mp3")
    assertThat(includePattern.matches("a.mp3a")).isFalse()
    assertThat(includePattern.matches("abc.mp3a")).isFalse()
    assertThat(includePattern.matches("mp3")).isFalse()
    assertThat(includePattern.matches("amp3")).isFalse()
    assertThat(includePattern.matches("abmp3")).isFalse()
    assertThat(includePattern.matches("a/bmp3")).isFalse()
    assertThat(includePattern.matches("abc/dmp3")).isFalse()
    assertThat(includePattern.matches("/abc/dmp3")).isFalse()
  }

  @Test
  fun multiplePatternsInName() {
    val includePattern = IncludePattern("a*b*.mp3")
    assertThat(includePattern.matches("ab.mp3")).isTrue()
    assertThat(includePattern.matches("axbx.mp3")).isTrue()
    assertThat(includePattern.matches("axxxb.mp3")).isTrue()
    assertThat(includePattern.matches("abxxx.mp3")).isTrue()
    assertThat(includePattern.matches("ba.mp3")).isFalse()
    assertThat(includePattern.matches("ab")).isFalse()
    assertThat(includePattern.matches("ba")).isFalse()
    assertThat(includePattern.matches("a/b.mp3")).isFalse()
    assertThat(includePattern.matches("ax/bx.mp3")).isFalse()
    assertThat(includePattern.matches("axbx/c.mp3")).isFalse()
  }

  @Test
  fun multipleStarStarPatterns() {
    val includePattern = IncludePattern("a/**/b/**/c")
    assertThat(includePattern.matches("a/b/c")).isTrue()
    assertThat(includePattern.matches("/a/b/c")).isTrue()
    assertThat(includePattern.matches("a/b/c/b/c")).isTrue()
    assertThat(includePattern.matches("a/b/c/a/b/c")).isTrue()
    assertThat(includePattern.matches("a/x/b/c")).isTrue()
    assertThat(includePattern.matches("a/b/x/c")).isTrue()
    assertThat(includePattern.matches("a/b/b/b/b/c/c/c")).isTrue()
    assertThat(includePattern.matches("a/c/b")).isFalse()
    assertThat(includePattern.matches("a/b/c/d")).isFalse()
    assertThat(includePattern.matches("a/b/cc")).isFalse()
    assertThat(includePattern.matches("aa/b/c")).isFalse()
    assertThat(includePattern.matches("a/bb/c")).isFalse()
    assertThat(includePattern.matches("a/b/cc")).isFalse()
    assertThat(includePattern.matches("a/b/c/")).isFalse()
  }

  @Test
  fun starPatternAtStart() {
    val includePattern = IncludePattern("**/a")
    assertThat(includePattern.matches("a")).isTrue()
    assertThat(includePattern.matches("b/a")).isTrue()
    assertThat(includePattern.matches("aa")).isFalse()
    assertThat(includePattern.matches("a/")).isFalse()
    assertThat(includePattern.matches("b/a/")).isFalse()
  }

  @Test
  fun starPatternAtEnd() {
    val includePattern = IncludePattern("a/**")
    assertThat(includePattern.matches("a")).isTrue()
    assertThat(includePattern.matches("a/b")).isTrue()
    assertThat(includePattern.matches("/a")).isTrue()
    assertThat(includePattern.matches("/a/")).isTrue()
    assertThat(includePattern.matches("/a/b")).isTrue()
    assertThat(includePattern.matches("/a/b/")).isTrue()
    assertThat(includePattern.matches("a/b/c")).isTrue()
    assertThat(includePattern.matches("a/b/c/")).isTrue()
    assertThat(includePattern.matches("/a/b/c")).isTrue()
    assertThat(includePattern.matches("/a/b/c/")).isTrue()
    assertThat(includePattern.matches("aa")).isFalse()
    assertThat(includePattern.matches("x/a")).isFalse()
    assertThat(includePattern.matches("./a")).isFalse()
  }
}
