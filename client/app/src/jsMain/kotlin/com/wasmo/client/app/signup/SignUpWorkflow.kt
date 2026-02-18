package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.WasmoApi
import com.wasmo.client.app.FormScreen
import com.wasmo.client.app.FormState
import com.wasmo.client.app.LocalFormState
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
  var formState by remember { mutableStateOf(FormState.Ready) }

  CompositionLocalProvider(LocalFormState provides formState) {
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
          SignUpCredentials { event ->
            formState = FormState.Busy
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
              } catch (e: Exception) {
                console.log("linkEmailAddress failed: $e")
              } finally {
                formState = FormState.Ready
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
}

interface SignUpWorkflowEvent {
  object Finished : SignUpWorkflowEvent
}
