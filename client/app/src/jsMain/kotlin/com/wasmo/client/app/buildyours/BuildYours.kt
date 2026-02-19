package com.wasmo.client.app.buildyours


import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wasmo.client.app.Checkbox
import com.wasmo.client.app.FinePrint
import com.wasmo.client.app.FormScreen
import com.wasmo.client.app.FormState
import com.wasmo.client.app.LocalFormState
import com.wasmo.client.app.PrimaryButton
import com.wasmo.client.app.SecondaryButton
import com.wasmo.client.app.SectionTitle
import com.wasmo.client.app.SmallText
import com.wasmo.client.app.TextField
import com.wasmo.compose.Toolbar
import com.wasmo.compose.ToolbarTitle
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.builders.InputAttrsScope
import org.jetbrains.compose.web.css.AlignSelf
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.alignSelf
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.paddingBottom
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul
import org.w3c.dom.HTMLDivElement

@Composable
fun BuildYoursScreen(
  showBuildForm: Boolean,
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  eventListener: (BuildYoursScreenEvent) -> Unit,
) {
  var formState by remember { mutableStateOf(FormState.Ready) }
  var nameState by remember { mutableStateOf("jesse99") }
  var emailState by remember { mutableStateOf("jesse@swank.ca") }

  CompositionLocalProvider(LocalFormState provides formState) {
    FormScreen(
      attrs = {
        classes("BuildYoursScreen")
        style {
          paddingBottom(48.px)
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
            alignSelf(AlignSelf.Center)
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
            marginBottom(36.px)
          }
        },
      ) {
        Text("Your Cloud Computer")
      }

      if (showBuildForm) {
        SectionTitle {
          Text("Give it a name:")
        }
      }

      WasmoNameField(
        attrs = {
        },
        inputAttrs = {
          value(nameState)
          onInput { event ->
            nameState = event.value
          }
        },
      )

      if (showBuildForm) {
        SmallText {
          Text("jesse99.wasmo.com is available.")
        }
        SmallText {
          Text("Names may use lowercase a-z characters and 0-9 numbers. No spaces or punctuation!")
        }

        SectionTitle {
          Text("Preinstall some apps:")
        }

        Checkbox(
          attrs = {},
          label = "Cloud Photo Library",
          inputAttrs = {
            checked(true)
          },
        ) {
          P {
            Text("Our photo management is just as good as big tech, but built to serve you and not a stock price.")
          }
          P {
            Text("It has everything you need:")
          }
          Ul {
            Li {
              Text("Import from Google or Apple")
            }
            Li {
              Text("Automatically tag for quick search")
            }
            Li {
              Text("Sync your phone to the cloud")
            }
          }
          P {
            Text($$"Wasmo.com charges $5 CAD / month for each 500 GiB of storage. That’s enough for most photo libraries.")
          }
        }

        Checkbox(
          attrs = {},
          label = "Cloud Music Library",
          inputAttrs = {
            checked(true)
          },
        ) {
          P {
            Text("The big streaming services aren’t paying artists enough. Buy music from the bands you love and listen to it on all of your devices.")
          }
        }

        Checkbox(
          attrs = {},
          label = "Cloud Audiobooks Library",
          inputAttrs = {
            checked(true)
          },
        ) {
          P {
            Text("Keeps your audiobook library in the cloud.")
          }
        }

        Checkbox(
          attrs = {},
          label = "Smart Home",
          inputAttrs = {
            checked(true)
          },
        ) {
          P {
            Text("Secure access to your smart home from anywhere.")
          }
        }

        Checkbox(
          attrs = {},
          label = "Plus a growing ecosystem",
          inputAttrs = {
            checked(true)
          },
        ) {
          P {
            Text("Wasmo has a capable SDK so anyone can build new apps for your cloud computer.")
          }
          P {
            Text("Your cloud computer is secure by construction. It’s always safe to try new apps.")
          }
          P {
            Text("We don’t do app review gatekeeping. It’s your computer and you can do what you like with it.")
          }
        }

        SectionTitle {
          Text("Start with this:")
        }

        Checkbox(
          attrs = {},
          type = InputType.Radio,
          label = "Wasmo Standard",
          inputAttrs = {
            checked(true)
          },
        ) {
          P {
            Text($$"$5 CAD / month")
            Br()
            Text("500 GiB of storage")
            Br()
            Text("Standard performance")
          }
          FinePrint {
            Text($$"You can buy additional storage later. $5 per 500 GiB.")
          }
        }

        SectionTitle {
          Text("Make it yours:")
        }

        TextField(
          label = "Email Address",
        ) {
          value(emailState)
          onInput { event ->
            emailState = event.value
          }
        }
        SmallText(
          attrs = {
            style {
              marginBottom(32.px)
            }
          }
        ) {
          Text("We’ll email you a link to access your Wasmo. We’ll also invite you to set up a passkey.")
        }
      }

      PrimaryButton(
        attrs = {
          style {
            marginTop(24.px)
            marginBottom(24.px)
          }
          onClick {
            eventListener(
              when {
                showBuildForm -> BuildYoursScreenEvent.ClickCheckOut
                else -> BuildYoursScreenEvent.ClickBuildYours
              },
            )
          }
        },
      ) {
        Text(
          when {
            showBuildForm -> "Check Out"
            else -> "Build Yours"
          },
        )
      }

      if (showBuildForm) {
        SecondaryButton(
          attrs = {
            onClick {
              eventListener(
                BuildYoursScreenEvent.ClickQuestions,
              )
            }
          },
        ) {
          Text("Questions")
        }
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

sealed interface BuildYoursScreenEvent {
  object ClickBuildYours : BuildYoursScreenEvent
  object ClickCheckOut : BuildYoursScreenEvent
  object ClickQuestions : BuildYoursScreenEvent
}
