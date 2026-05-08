package com.wasmo.client.app

import androidx.compose.runtime.compositionLocalOf

enum class FormState {
  Ready,
  Busy,
}

val LocalFormState = compositionLocalOf { FormState.Ready }

