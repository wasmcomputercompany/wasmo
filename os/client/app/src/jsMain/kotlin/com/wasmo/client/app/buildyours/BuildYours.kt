package com.wasmo.client.app.buildyours


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wasmo.compose.Button
import com.wasmo.compose.Checkbox
import com.wasmo.compose.Column
import com.wasmo.compose.PageLayout
import com.wasmo.compose.TextField
import com.wasmo.identifiers.ComputerSlug
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Section
import org.jetbrains.compose.web.dom.Small
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul
import org.w3c.dom.HTMLDivElement

@Composable
fun BuildYoursScreen(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  eventListener: (BuildYoursScreenEvent) -> Unit,
) {
  var nameState by remember { mutableStateOf("jesse99") }

  PageLayout(
    attrs = attrs,
  ) { contentAttrs ->
    Column(
      attrs = {
        classes("ContentWidth")
        contentAttrs()
      },
    ) {

      Section {
        H2 {
          Text("Give it a name:")
        }

        TextField(
          helperText = "$nameState.wasmo.com is available.",
          groupedInputs = {
            Input(
              type = InputType.Text,
              attrs = {
                disabled()
                defaultValue(".wasmo.com")
              },
            )
          },
          inputAttrs = {
            value(nameState)
            onInput { event ->
              nameState = event.value
            }
          },
        )

        P {
          Text("Names may use lowercase a-z characters and 0-9 numbers. No spaces or punctuation!")
        }
      }

      Section {
        H2 {
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
      }

      // TODO: Make options and their pricing (including free) configurable by the server.
      Section {
        H2 {
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
          Small {
            Text($$"You can buy additional storage later. $5 per 500 GiB.")
          }
        }
      }

      Section {
        Button(
          attrs = {
            onClick {
              eventListener(BuildYoursScreenEvent.ClickCreateComputer(ComputerSlug(nameState)))
            }
          },
        ) {
          Text("Create Computer")
        }
      }

      Section {
        Button(
          outline = true,
          attrs = {
            onClick {
              eventListener(BuildYoursScreenEvent.ClickQuestions)
            }
          },
        ) {
          Text("Questions")
        }
      }
    }
  }
}

sealed interface BuildYoursScreenEvent {
  data class ClickCreateComputer(
    val slug: ComputerSlug,
  ) : BuildYoursScreenEvent

  object ClickQuestions : BuildYoursScreenEvent
}
