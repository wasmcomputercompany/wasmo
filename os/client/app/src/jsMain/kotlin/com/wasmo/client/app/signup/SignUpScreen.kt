package com.wasmo.client.app.signup


import androidx.compose.runtime.Composable
import com.wasmo.client.app.FormScreen
import com.wasmo.client.app.PrimaryButton
import com.wasmo.client.app.TextField
import com.wasmo.compose.Toolbar
import com.wasmo.compose.ToolbarTitle
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun SignUpScreen(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  eventListener: (AuthScreenEvent) -> Unit,
) {
  FormScreen(
    attrs = {
      classes("AuthScreen")
      attrs()
    },
  ) {
    SignUpToolbar()

    TextField(
      label = "Email Address",
    ) {
      onInput { event ->
        eventListener(AuthScreenEvent.EditEmailAddress(event.value))
      }
    }

    PrimaryButton(
      attrs = {
        style {
          marginTop(24.px)
          marginBottom(24.px)
        }
        onClick {
          eventListener(AuthScreenEvent.ClickSendCode)
        }
      },
    ) {
      Text("Send Code")
    }
  }
}

@Composable
fun SignUpToolbar(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
) {
  Toolbar(
    attrs = {
      style {
        marginBottom(8.px)
      }
      attrs()
    },
    title = { toolbarChildAttrs ->
      ToolbarTitle(
        attrs = toolbarChildAttrs,
      ) {
        Text("Sign Up")
      }
    },
  )
}

sealed interface AuthScreenEvent {
  object ClickSendCode : AuthScreenEvent

  data class EditEmailAddress(
    val emailAddress: String,
  ) : AuthScreenEvent
}

