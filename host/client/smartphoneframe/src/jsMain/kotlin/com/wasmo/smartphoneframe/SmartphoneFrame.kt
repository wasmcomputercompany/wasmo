package com.wasmo.smartphoneframe

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color.black
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.overflow
import org.jetbrains.compose.web.css.overflowX
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

/**
 * An isometric projection of a smartphone displaying another web page.
 *
 * This uses CSS to transform a flat iframe wrapper into something with a slight bit more depth.
 */
@Composable
fun SmartphoneFrame(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit,
  content: @Composable DOMScope<HTMLDivElement>.(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) -> Unit,
) {
  Div(
    attrs = {
      classes("SmartphoneFrame")
      style {
        margin(16.px, 0.px)
        width(100.percent)
        height(536.px)
        overflowX("clip")
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Center)
      }
      attrs()
    },
  ) {
    Div(
      attrs = {
        style {
          width(380.px)
          height(536.px)
          position(Position.Relative)
          property("z-index", "0")
        }
      },
    ) {
      FlatSmartphoneFrame(
        attrs = {
          style {
            property(
              "transform",
              "translateY(-82px) translateX(-11px) scale(0.7) skew(-10deg, -6deg)",
            )
            position(Position.Absolute)
          }
        },
      ) {
      }
      FlatSmartphoneFrame(
        attrs = {
          style {
            property(
              "transform",
              "translateY(-87px) translateX(-16px) scale(0.7) skew(-10deg, -6deg)",
            )
            position(Position.Absolute)
          }
        },
      ) {
      }
      FlatSmartphoneFrame(
        attrs = {
          style {
            property(
              "transform",
              "translateY(-92px) translateX(-19px) scale(0.7) skew(-10deg, -6deg)",
            )
            position(Position.Absolute)
          }
        },
      ) {
        StatusBar()
        content {
          style {
            flex("100 100 0")
          }
        }
        BottomBar()
      }
    }
  }
}

@Composable
private fun FlatSmartphoneFrame(
  attrs: AttrsScope<HTMLElement>.() -> Unit,
  content: @Composable DOMScope<HTMLDivElement>.() -> Unit,
) {
  Div(
    attrs = {
      style {
        width(400.px)
        height(700.px)
        backgroundColor(black)
        border(5.px, LineStyle.Solid, black)
        borderRadius(25.px)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        overflow("clip")
      }
      attrs()
    },
  ) {
    content()
  }
}

@Composable
private fun StatusBar() {
  Div(
    attrs = {
      style {
        backgroundColor(black)
        height(30.px)
        borderRadius(25.px, 25.px, 0.px, 0.px)
      }
    },
  )
}

@Composable
private fun BottomBar() {
  Div(
    attrs = {
      style {
        backgroundColor(black)
        height(20.px)
        borderRadius(0.px, 0.px, 25.px, 25.px)
      }
    },
  )
}
