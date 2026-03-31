package com.wasmo.journal.app

import androidx.compose.runtime.Composable
import com.wasmo.journal.api.Visibility
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.size
import org.jetbrains.compose.web.attributes.value
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea
import org.w3c.dom.HTMLDivElement

@Composable
fun EditEntry(
  saveState: SaveState,
  title: String,
  slug: String,
  body: String,
  visibility: Visibility,
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  eventListener: (EditPostEvent) -> Unit,
) {
  Div(
    attrs = {
      style {
        width(100.percent)
        height(100.percent)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Stretch)
        justifyContent(JustifyContent.Start)
      }
      attrs()
    },
  ) {
    when (saveState) {
      is SaveState.Error -> {
        P {
          Text("save error")
        }
      }
      SaveState.Saved -> {
        P {
          Text("saved")
        }
      }
      SaveState.Saving -> {
        P {
          Text("saving...")
        }
      }
    }

    Text("title")
    Input(
      type = InputType.Text,
    ) {
      size(10)
      defaultValue(title)
      onInput { event ->
        eventListener(EditPostEvent.EditTitle(event.value))
      }
    }

    Text("slug")
    TextArea(
      attrs = {
        defaultValue(slug)
        onInput { event ->
          eventListener(EditPostEvent.EditSlug(event.value))
        }
      },
    )

    Text("body")
    TextArea(
      attrs = {
        defaultValue(body)
        onInput { event ->
          eventListener(EditPostEvent.EditBody(event.value))
        }
      },
    )

    when (visibility) {
      Visibility.Private -> {
        Button(
          attrs = {
            value("Publish")
            onClick {
              eventListener(EditPostEvent.ClickPublish)
            }
          },
        )
      }

      Visibility.Published -> {
        Button(
          attrs = {
            value("Unpublish")
            onClick {
              eventListener(EditPostEvent.ClickUnpublish)
            }
          },
        )
      }

      Visibility.Deleted -> {
      }
    }
  }
}

sealed interface SaveState {
  data object Saved : SaveState
  data object Saving : SaveState
  data class Error(val message: String) : SaveState
}

sealed interface EditPostEvent {
  data class EditTitle(val value: String): EditPostEvent
  data class EditSlug(val value: String): EditPostEvent
  data class EditBody(val value: String): EditPostEvent
  data object ClickPublish: EditPostEvent
  data object ClickUnpublish: EditPostEvent
}
