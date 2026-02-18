package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.WasmoApi
import com.wasmo.client.app.FormScreen
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLDivElement

@Composable
fun SignUpWorkflow(
  wasmoApi: WasmoApi,
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  eventListener: (SignUpWorkflowEvent) -> Unit,
) {
  var stepsCompleted by remember { mutableIntStateOf(0) }
  val scope = rememberCoroutineScope()
  var enabled by remember { mutableStateOf(true) }

  FormScreen(
    attrs = attrs,
  ) {
    SignUpToolbar()
    SignUpSegmentedProgressBar(
      stepsCompleted = stepsCompleted + 1,
      stepCount = 5,
    )

    when (stepsCompleted) {
      0 -> {
        SignUpIntro { _ ->
          stepsCompleted++
        }
      }

      1 -> {
        SignUpCredentials(
          enabled = enabled,
        ) { event ->
          enabled = false
          scope.launch {
            try {
              if (event is SignUpCredentialsEvent.CreateAccount) {
                val result = wasmoApi.linkEmailAddress(
                  LinkEmailAddressRequest(
                    unverifiedEmailAddress = event.email,
                  ),
                )
                if (result.challengeSent) {
                  stepsCompleted++
                }
              }
            } finally {
              enabled = true
            }
          }
        }
      }

      2 -> {
        SignUpPayment { _ ->
          stepsCompleted++
        }
      }

      3 -> {
        SignUpCreateWasmo { _ ->
          stepsCompleted++
        }
      }

      4 -> {
        SignUpChallengeCode { _ ->
          stepsCompleted = 0
          eventListener(SignUpWorkflowEvent.Finished)
        }
      }
    }
  }
}

interface SignUpWorkflowEvent {
  object Finished : SignUpWorkflowEvent
}
