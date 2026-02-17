package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wasmo.client.app.FormScreen
import com.wasmo.client.app.PrimaryButton
import com.wasmo.client.app.TextField
import com.wasmo.compose.ChildStyle
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun SignUpCreateWasmo(
  childStyle: ChildStyle,
  eventListener: (SignUpCreateWasmoEvent) -> Unit,
) {
  var nameState by remember { mutableStateOf("jesse99") }

  FormScreen(
    childStyle = childStyle,
  ) {
    SignUpToolbar(
      childStyle = ChildStyle {},
    )
    SignUpSegmentedProgressBar(
      stepsCompleted = 4,
      stepCount = 5,
    )
    P {
      Text("Name your Wasmo.")
    }
    Div(
      attrs = {
        style {
          display(DisplayStyle.Flex)
          flexDirection(FlexDirection.Row)
        }
      },
    ) {
      TextField(
        childStyle = ChildStyle {
          flex("100 100 0")
        },
      ) {
        value(nameState)
        onInput { event ->
          nameState = event.value
        }
      }
      Div(
        attrs = {
          classes("TextFieldSuffix")
        },
      ) {
        Text(".wasmo.com")
      }
    }
    P(
      attrs = {
        style {
          marginTop(24.px)
        }
      },
    ) {
      Text("Names may use lowercase a-z characters and 0-9 numbers. No spaces or punctuation!")
    }
    PrimaryButton(
      childStyle = ChildStyle {
        marginTop(24.px)
        marginBottom(24.px)
      },
    ) {
      value("Create Wasmo")
      onClick {
        eventListener(
          SignUpCreateWasmoEvent.CreateWasmo(
            name = nameState,
          ),
        )
      }
    }
  }
}

sealed interface SignUpCreateWasmoEvent {
  data class CreateWasmo(
    val name: String,
  ) : SignUpCreateWasmoEvent
}
