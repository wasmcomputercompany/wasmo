package com.wasmo.testing.apps

import com.wasmo.computers.AppCatalog
import com.wasmo.computers.AppCatalog.Entry
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Note that [MusicApp] and [SnakeApp] are installed by default. [RecipesApp] is not!
 */
@Inject
@SingleIn(OsScope::class)
class SampleApps(
  val music: MusicApp.Factory,
  val snake: SnakeApp.Factory,
  val recipes: RecipesApp.Factory,
) {
  val appCatalog = AppCatalog(
    entries = listOf(music.publishedApp, snake.publishedApp).map {
      Entry(
        wasmoFileAddress = it.wasmoFileAddress,
        slug = it.slug,
      )
    },
  )
}
