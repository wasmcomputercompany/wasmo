package com.wasmo.compose

import androidx.compose.runtime.Composable
import androidx.compose.web.events.SyntheticMouseEvent
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.DOMScope
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

@Composable
fun OverlayContainer(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit,
  showScrim: Boolean,
  onClickScrim: ((SyntheticMouseEvent) -> Unit)? = null,
  overlay: @Composable DOMScope<HTMLDivElement>.(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) -> Unit,
  content: @Composable DOMScope<HTMLDivElement>.(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) -> Unit,
) {
  Zstack(
    attrs = attrs,
  ) { zstackChildAttrs ->
    content(zstackChildAttrs)

    Scrim(
      attrs = zstackChildAttrs,
      visible = showScrim,
      onClick = onClickScrim,
    )

    overlay(zstackChildAttrs)
  }
}
