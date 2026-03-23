package com.wasmo.client.app.computer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wasmo.client.app.Form
import com.wasmo.client.app.FormState
import com.wasmo.client.app.LocalFormState
import com.wasmo.client.app.PrimaryButton
import com.wasmo.client.app.SmallText
import com.wasmo.client.app.TextField
import com.wasmo.compose.Dialog
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun InstallAppDialog(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  model: InstallAppDialogModel?,
  eventListener: (InstallAppDialogEvent) -> Unit,
) {
  val formState = model?.formState ?: FormState.Busy
  var appUrlState by remember {
    mutableStateOf("http://wasmo.localhost:8080/journal/journal.wasmo.toml")
  }

  CompositionLocalProvider(LocalFormState provides formState) {
    Dialog(
      attrs = attrs,
      visible = model != null,
      onDismiss = {
        eventListener(InstallAppDialogEvent.ClickDismiss)
      },
      content = { dialogChildAttrs ->
        Form(
          attrs = dialogChildAttrs,
        ) {
          TextField(
            label = "App URL",
          ) {
            value(appUrlState)
            onInput { event ->
              appUrlState = event.value
            }
          }
          SmallText(
            attrs = {
              style {
                marginBottom(16.px)
              }
            },
          ) {
            Text("Paste the URL of the app to install.")
          }

          PrimaryButton(
            attrs = {
              style {
                marginTop(24.px)
                marginBottom(24.px)
              }
              onClick {
                eventListener(InstallAppDialogEvent.ClickInstall(appUrlState))
              }
            },
          ) {
            Text("Install")
          }
        }
      },
    )
  }
}

sealed interface InstallAppDialogEvent {
  object ClickDismiss : InstallAppDialogEvent
  data class ClickInstall(val appUrl: String) : InstallAppDialogEvent
}
