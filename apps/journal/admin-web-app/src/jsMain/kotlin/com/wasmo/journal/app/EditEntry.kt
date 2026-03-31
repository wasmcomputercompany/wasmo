package com.wasmo.journal.app

import androidx.compose.runtime.Composable
import com.wasmo.journal.api.Visibility
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.size
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea
import org.w3c.dom.HTMLDivElement

@Composable
fun EditEntry(
  syncState: SyncState,
  title: String,
  slug: String,
  body: String,
  visibility: Visibility,
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  eventListener: (EditEntryEvent) -> Unit,
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
    A(
      attrs = {
        onClick {
          eventListener(EditEntryEvent.ClickBack)
        }
      }
    ) {
      Text("<- Back")
    }

    when (syncState) {
      SyncState.Loading -> {
        P {
          Text("loading...")
        }
      }
      is SyncState.Error -> {
        P {
          Text("save error")
        }
      }
      SyncState.Ready -> {
        P {
          Text("saved")
        }
      }
      SyncState.Dirty -> {
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
        eventListener(EditEntryEvent.EditTitle(event.value))
      }
    }

    Text("slug")
    Input(
      type = InputType.Text,
    ) {
      size(10)
      defaultValue(slug)
      onInput { event ->
        eventListener(EditEntryEvent.EditSlug(event.value))
      }
    }

    Text("body")
    TextArea(
      attrs = {
        defaultValue(body)
        style {
          flex(100, 100, 0.px)
        }
        onInput { event ->
          eventListener(EditEntryEvent.EditBody(event.value))
        }
      },
    )

    when (visibility) {
      Visibility.Private -> {
        Button(
          attrs = {
            onClick {
              eventListener(EditEntryEvent.ClickPublish)
            }
          },
        ) {
          Text("Publish")
        }
      }

      Visibility.Published -> {
        Button(
          attrs = {
            onClick {
              eventListener(EditEntryEvent.ClickUnpublish)
            }
          },
        ) {
          Text("Unpublish")
        }
      }

      Visibility.Deleted -> {
      }
    }
  }
}

sealed interface EditEntryEvent {
  data object ClickBack: EditEntryEvent
  data class EditTitle(val value: String): EditEntryEvent
  data class EditSlug(val value: String): EditEntryEvent
  data class EditBody(val value: String): EditEntryEvent
  data object ClickPublish: EditEntryEvent
  data object ClickUnpublish: EditEntryEvent
}
