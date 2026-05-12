package com.wasmo.client.app.computer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wasmo.compose.Button
import com.wasmo.compose.Column
import com.wasmo.compose.Dialog
import com.wasmo.compose.DialogCloseHeader
import com.wasmo.compose.TextField
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.Footer
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement

@Composable
fun InstallAppDialog(
  attrs: AttrsScope<HTMLElement>.() -> Unit = {},
  model: InstallAppDialogModel?,
  eventListener: (InstallAppDialogEvent) -> Unit,
) {
  var appUrlState by remember {
    mutableStateOf("http://wasmo.localhost:8080/journal/journal.wasmo")
  }
  var appSlugState by remember {
    mutableStateOf("journal")
  }

  Dialog(
    attrs = attrs,
    visible = model != null,
    content = { dialogChildAttrs ->
      DialogCloseHeader(
        onDismiss = {
          eventListener(InstallAppDialogEvent.ClickDismiss)
        },
      )
      Column(
        attrs = dialogChildAttrs,
      ) {
        TextField(
          label = "App URL",
          caption = "Paste the URL of the app to install.",
          inputAttrs = {
            value(appUrlState)
            onInput { event ->
              appUrlState = event.value
            }
          },
        )

        TextField(
          label = "App Name",
          inputAttrs = {
            value(appSlugState)
            onInput { event ->
              appSlugState = event.value
            }
          },
        )

        Footer {
          Button(
            attrs = {
              onClick {
                eventListener(
                  InstallAppDialogEvent.ClickInstall(
                    appUrl = appUrlState,
                    slug = appSlugState,
                  ),
                )
              }
            },
          ) {
            Text("Install")
          }
        }
      }
    },
  )
}

sealed interface InstallAppDialogEvent {
  object ClickDismiss : InstallAppDialogEvent
  data class ClickInstall(
    val appUrl: String,
    val slug: String,
  ) : InstallAppDialogEvent
}
