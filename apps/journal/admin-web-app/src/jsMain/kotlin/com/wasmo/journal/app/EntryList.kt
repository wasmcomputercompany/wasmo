package com.wasmo.journal.app

import androidx.compose.runtime.Composable
import com.wasmo.journal.api.EntrySummary
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.disabled
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
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun EntryList(
  entries: List<EntrySummary>,
  publishState: PublishStateViewModel,
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  eventListener: (EntryListEvent) -> Unit,
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
          eventListener(EntryListEvent.ViewSite)
        }
      },
    ) {
      Text("View Site")
    }

    Button(
      attrs = {
        if (!publishState.canRequestPublish) {
          disabled()
        }
        onClick {
          eventListener(EntryListEvent.PublishSite)
        }
      },
    ) {
      Text("Publish Site")
    }

    Button(
      attrs = {
        onClick {
          eventListener(EntryListEvent.NewEntry)
        }
      },
    ) {
      Text("New Entry")
    }

    for (entry in entries) {
      EntryRow(entry, eventListener)
    }
  }
}

@Composable
fun EntryRow(
  entrySummary: EntrySummary,
  eventListener: (EntryListEvent) -> Unit,
) {
  Div(
    attrs = {
      style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        alignItems(AlignItems.Stretch)
        justifyContent(JustifyContent.Start)
      }
      onClick {
        eventListener(EntryListEvent.ClickEntry(entrySummary.token))
      }
    },
  ) {
    P {
      Text(entrySummary.title)
    }
  }
}

sealed interface EntryListEvent {
  data object NewEntry : EntryListEvent
  data object ViewSite : EntryListEvent
  data object PublishSite : EntryListEvent
  data class ClickEntry(val token: String) : EntryListEvent
}
