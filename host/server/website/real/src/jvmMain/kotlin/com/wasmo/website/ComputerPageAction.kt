package com.wasmo.website

import com.wasmo.accounts.AccountStore
import com.wasmo.accounts.Client
import com.wasmo.api.AppSlug
import com.wasmo.api.ComputerSlug
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.InstalledApp
import com.wasmo.app.db.WasmoDbService

class ComputerPageAction(
  private val client: Client,
  private val accountStoreFactory: AccountStore.Factory,
  private val appPageFactory: ServerAppPage.Factory,
  private val wasmoDbService: WasmoDbService,
) {
  fun get(computerSlug: ComputerSlug): ServerAppPage {
    val accountStore = accountStoreFactory.create(client)
    val computerSnapshot = ComputerSnapshot(
      slug = computerSlug,
      apps = listOf(
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
      ),
    )

    return wasmoDbService.transactionWithResult(noEnclosing = true) {
      val accountSnapshot = accountStore.snapshot()
      appPageFactory.create(
        accountSnapshot = accountSnapshot,
        computerSnapshot = computerSnapshot,
      )
    }
  }
}
