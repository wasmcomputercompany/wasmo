package com.wasmo.client.app.buildyours


import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wasmo.client.app.FormScreen
import com.wasmo.client.app.FormState
import com.wasmo.client.app.LocalFormState
import com.wasmo.client.app.PrimaryButton
import com.wasmo.client.app.TextField
import com.wasmo.compose.Toolbar
import com.wasmo.compose.ToolbarTitle
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.builders.InputAttrsScope
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.background
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun BuildYoursFormScreen(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
) {
  var formState by remember { mutableStateOf(FormState.Ready) }
  var nameState by remember { mutableStateOf("jesse99") }

  CompositionLocalProvider(LocalFormState provides formState) {
    FormScreen(
      attrs = {
        style {
          background("#4c6d81")
        }
        attrs()
      },
    ) {
      BuildYoursToolbar()

      Img(
        src = "/assets/wasmo1000x300.svg",
        alt = "Wasmo",
        attrs = {
          style {
            property("width", "350px")
            property("height", "105px")
          }
        },
      )

      H2(
        attrs = {
          style {
            textAlign("center")
            marginTop(24.px)
            marginBottom(24.px)
          }
        },
      ) {
        Text("Your Cloud Computer")
      }

      WasmoNameField(
        attrs = {
          style {
            marginTop(24.px)
          }
        },
        inputAttrs = {
          value(nameState)
          onInput { event ->
            nameState = event.value
          }
        },
      )

      PrimaryButton(
        attrs = {
          style {
            marginTop(24.px)
            marginBottom(24.px)
          }
          onClick {
          }
        },
      ) {
        Text("Build Yours")
      }
    }
  }
}

@Composable
private fun WasmoNameField(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit,
  inputAttrs: InputAttrsScope<String>.() -> Unit,
) {
  Div(
    attrs = {
      style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
      }
      attrs()
    },
  ) {
    TextField(
      attrs = {
        style {
          flex("100 100 0")
        }
      },
      inputAttrs = inputAttrs,
    )
    Div(
      attrs = {
        classes("TextFieldSuffix")
      },
    ) {
      Text(".wasmo.com")
    }
  }
}

@Composable
fun BuildYoursToolbar(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
) {
  Toolbar(
    attrs = {
      style {
        marginBottom(8.px)
      }
      attrs()
    },
    title = {
      ToolbarTitle {
        Text("")
      }
    },
  )
}
