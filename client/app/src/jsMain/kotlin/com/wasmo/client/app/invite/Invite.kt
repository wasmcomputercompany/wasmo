package com.wasmo.client.app.invite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wasmo.client.app.FormScreen
import com.wasmo.client.app.FormState
import com.wasmo.client.app.FormWasmoLogo
import com.wasmo.client.app.LocalFormState
import com.wasmo.client.app.PrimaryButton
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.paddingBottom
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun InviteScreen(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  eventListener: (InviteEvent) -> Unit,
) {
  var formState by remember { mutableStateOf(FormState.Ready) }
  CompositionLocalProvider(LocalFormState provides formState) {
    FormScreen(
      attrs = {
        classes("InviteScreen")
        style {
          paddingBottom(48.px)
        }
        attrs()
      },
    ) {
      FormWasmoLogo()

      H2(
        attrs = {
          style {
            textAlign("center")
            marginTop(24.px)
            marginBottom(36.px)
          }
        },
      ) {
        Text("Your Cloud Computer")
      }

      P {
        Text("You've been invited to look around our wildly incomplete website. Please send feedback to jessewilson.99 on Signal.")
      }

      PrimaryButton(
        attrs = {
          style {
            marginTop(24.px)
            marginBottom(24.px)
          }
          onClick {
            eventListener(
              InviteEvent.ClickAccept
            )
          }
        }
      ) {
        Text("Accept Invite")
      }
    }
  }
}

sealed interface InviteEvent {
  object ClickAccept : InviteEvent
}
