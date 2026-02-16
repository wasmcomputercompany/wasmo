package com.wasmo.compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.StyleScope

interface ChildStyle {
  context(styleScope: StyleScope)
  operator fun invoke()
}

inline fun ChildStyle(
  crossinline block: StyleScope.() -> Unit,
) = object : ChildStyle {
  context(styleScope: StyleScope)
  override fun invoke() {
    styleScope.block()
  }
}

typealias ComposableElement = @Composable (childStyle: ChildStyle) -> Unit

@Composable
inline operator fun ComposableElement.invoke(
  crossinline block: StyleScope.() -> Unit,
) {
  this(ChildStyle(block))
}
