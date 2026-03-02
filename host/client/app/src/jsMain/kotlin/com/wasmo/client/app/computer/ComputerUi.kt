package com.wasmo.client.app.computer

import androidx.compose.runtime.Composable
import com.wasmo.api.AppSlug
import com.wasmo.api.ComputerSlug
import com.wasmo.api.InstalledApp
import com.wasmo.api.routes.AppRoute
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.framework.Ui
import com.wasmo.launcher.Icon
import com.wasmo.launcher.LauncherIconList
import com.wasmo.launcher.LauncherScreen
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

class ComputerUi(
  val slug: ComputerSlug,
  val router: Router,
  val apps: List<InstalledApp>,
) : Ui {
  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    LauncherScreen {
      LauncherIconList {
        for (app in apps) {
          Icon(app.label, app.maskableIconUrl) {
            router.goTo(AppRoute(slug, app.slug), TransitionDirection.PUSH)
          }
        }
      }
    }
  }

  class Factory(
    val router: Router,
  ) {
    private val apps = listOf(
      InstalledApp(
        label = "Files",
        slug = AppSlug("files"),
        maskableIconUrl = "/assets/launcher/sample-folder.svg",
      ),
      InstalledApp(
        label = "Library",
        slug = AppSlug("library"),
        maskableIconUrl = "/assets/launcher/sample-books.svg",
      ),
      InstalledApp(
        label = "Music",
        slug = AppSlug("music"),
        maskableIconUrl = "/assets/launcher/sample-headphones.svg",
      ),
      InstalledApp(
        label = "Photos",
        slug = AppSlug("photos"),
        maskableIconUrl = "/assets/launcher/sample-camera.svg",
      ),
      InstalledApp(
        label = "Pink Journal",
        slug = AppSlug("pink"),
        maskableIconUrl = "/assets/launcher/sample-flower.svg",
      ),
      InstalledApp(
        label = "Recipes",
        slug = AppSlug("recipes"),
        maskableIconUrl = "/assets/launcher/sample-pancakes.svg",
      ),
      InstalledApp(
        label = "Smart Home",
        slug = AppSlug("smart"),
        maskableIconUrl = "/assets/launcher/sample-home.svg",
      ),
      InstalledApp(
        label = "Snake",
        slug = AppSlug("snake"),
        maskableIconUrl = "/assets/launcher/sample-snake.svg",
      ),
      InstalledApp(
        label = "Writer",
        slug = AppSlug("writer"),
        maskableIconUrl = "/assets/launcher/sample-w.svg",
      ),
      InstalledApp(
        label = "Zap",
        slug = AppSlug("zap"),
        maskableIconUrl = "/assets/launcher/sample-z.svg",
      ),
    )

    fun create(route: ComputerHomeRoute) = ComputerUi(
      slug = route.slug,
      router = router,
      apps = apps,
    )
  }
}
