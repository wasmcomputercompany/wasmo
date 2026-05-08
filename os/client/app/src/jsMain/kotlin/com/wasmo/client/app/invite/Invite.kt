package com.wasmo.client.app.invite

import androidx.compose.runtime.Composable
import com.wasmo.compose.Button
import com.wasmo.compose.Column
import com.wasmo.compose.LargeWasmoLogo
import com.wasmo.compose.PageLayout
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.AlignSelf
import org.jetbrains.compose.web.css.alignSelf
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Section
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun InviteScreen(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  inviteState: InviteState,
  eventListener: (InviteEvent) -> Unit,
) {
  PageLayout(
    attrs = attrs,
  ) { contentAttrs ->
    Column(
      attrs = {
        classes("ContentWidth")
        contentAttrs()
      },
    ) {
      Section(
        attrs = {
          style {
            alignSelf(AlignSelf.Center)
          }
        },
      ) {
        LargeWasmoLogo()
        H2(
          attrs = {
            style {
              textAlign("center")
            }
          },
        ) {
          Text("Your Cloud Computer")
        }
      }

      Section {
        P {
          Text("You've been invited to look around our wildly incomplete website. Please send feedback to jessewilson.99 on Signal.")
        }
        Button(
          busy = inviteState == InviteState.ServerBusy || inviteState == InviteState.ClientBusy,
          disabled = inviteState != InviteState.ReadyToAccept && inviteState != InviteState.ReadyToSignIn,
          attrs = {
            onClick {
              eventListener(
                InviteEvent.ClickAccept,
              )
            }
          },
        ) {
          when (inviteState) {
            InviteState.ReadyToAccept -> Text("Accept Invite")
            InviteState.ReadyToSignIn -> Text("Sign In")
            InviteState.ClientBusy -> Text("...")
            InviteState.ServerBusy -> Text("...")
            InviteState.Failed -> Text("Failed")
          }
        }
      }
    }
  }
}

enum class InviteState {
  ReadyToAccept,
  ReadyToSignIn,
  ClientBusy,
  ServerBusy,
  Failed,
}

sealed interface InviteEvent {
  object ClickAccept : InviteEvent
}
