package com.wasmo.client.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wasmo.common.logging.ConsoleLogger
import com.wasmo.common.logging.Logger
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposableInBody

class WasmoClientApp(
  val logger: Logger = ConsoleLogger,
) {
  fun start() {
    logger.info("hello")

    renderComposableInBody {
      Home()
    }
  }
}

@Composable
fun Home() {
  var count: Int by remember { mutableStateOf(0) }

  Div({ style { padding(25.px) } }) {
    Button(
      attrs = {
        onClick { count -= 1 }
      },
    ) {
      Text("-")
    }

    Span({ style { padding(15.px) } }) {
      Text("$count")
    }

    Button(
      attrs = {
        onClick { count += 1 }
      },
    ) {
      Text("+")
    }
  }
}
