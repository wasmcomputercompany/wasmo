package com.wasmo.client.app

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.StyleScope

typealias ComposableElement = @Composable (childStyle: StyleScope.() -> Unit) -> Unit
