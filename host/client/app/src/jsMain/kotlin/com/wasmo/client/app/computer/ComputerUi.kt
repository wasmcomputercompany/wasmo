package com.wasmo.client.app.computer

import androidx.compose.runtime.Composable
import com.wasmo.client.framework.Ui
import com.wasmo.launcher.Icon
import com.wasmo.launcher.LauncherIconList
import com.wasmo.launcher.LauncherScreen
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

class ComputerUi(
  val slug: String,
) : Ui {
  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    LauncherScreen {
      LauncherIconList {
        Icon("Files", "/assets/launcher/sample-folder.svg")
        Icon("Library", "/assets/launcher/sample-books.svg")
        Icon("Music", "/assets/launcher/sample-headphones.svg")
        Icon("Photos", "/assets/launcher/sample-camera.svg")
        Icon("Pink Journal", "/assets/launcher/sample-flower.svg")
        Icon("Recipes", "/assets/launcher/sample-pancakes.svg")
        Icon("Smart Home", "/assets/launcher/sample-home.svg")
        Icon("Snake", "/assets/launcher/sample-snake.svg")
        Icon("Writer", "/assets/launcher/sample-w.svg")
        Icon("Zap", "/assets/launcher/sample-z.svg")
      }
    }
  }
}
