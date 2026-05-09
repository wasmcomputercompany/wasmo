Workflows
=========

We're building a framework for workflow UIs.

Goals
-----

* Very easy to build form-driven UIs, on both client and server.
* Support zero-step workflows ('delete a linked email address')
* Support multiple-step, complex workflows ('configure a new Homelab from scratch')
* Support skippable steps ('set a passcode for faster onboarding')
* Support external steps ('pay for the computer being created')
* Support end-of-workflow goals ('sign in, in order to navigate to an app')
* Easy to testable.
* Easy to perform workflows in service of other tests.

Non-Goals
---------

* Server-driven UI. Our only client is the web, and we can push new client code whenever we want.

User Stories
------------

Here's some hypothetical workflows that our framework should support.

### Sign Up

* Rocky navigates to 'https://wasmo.com/'.
* Rocky enters an email address.
* Rocky receives a challenge code in their email.
* Rocky enters a 6-digit code.
* Rocky is prompted to optionally set a passcode.
* Rocky presses 'Skip'.
* Rocky is presented a signed in page, 'https://wasmo.com/'.

### Create a computer

* Rocky navigates to 'https://wasmo.com/' and is signed in.
* Rocky clicks 'Create Computer'.
* Rocky enters a computer name, 'rocky11'.
* Rocky selects 'Wasmo Standard'.
* Rocky clicks 'Check Out'.
* Rocky completes the Stripe form to pay $5/month.
* Rocky is presented https://rocky11.wasmo.com/.

### Install an app

- Rocky navigates to 'https://rocky11.wasmo.com/'.
- Rocky clicks a button, 'Install App'.
- Rocky pastes an app's URL and presses 'Next'.
- Rocky is presented an error, 'App not found'.
- Rocky pastes a different URL and presses 'Next'.
- Rocky reviews the app’s requirements.
- Rocky approves the installation.
- Rocky is presented 'https://journal.rocky11.wasmo.com/'.

### Deep Link to Journal, Requiring Sign-In

Assume Homelab without any passwords.

* Rocky navigates to 'https://journal-rocky11.wasmo.local/admin/'.
* Rocky is redirected to a 'sign in' page.
* Rocky enters 'rocky' and presses 'Sign In'
* Rocky is presented 'https://journal-rocky11.wasmo.local/admin/'.

### Delete an app

* Rocky navigates to 'https://rocky11.wasmo.com/admin/'.
* Rocky long presses on the Journal icon.
* Rocky is presented a context menu.
* Rocky clicks 'delete'.
* Rocky sees the app icon go poof.

Model
-----

### Objective

This is where the user tells us what they want.

```kotlin
data object SignUpObjective : Input
```

Objectives may contain parameters. If parameters are null, that data might be collected in the
process of the workflow.

```kotlin
data class InstallAppObjective(
  val computer: ComputerSlug?,
  val appUrl: String?,
) : Input
```

### Step

A step represents the data to collect from a user. The `Step` type is a tree node, with leaf nodes
representing specific questions to answer or results to acknowledge, and internal nodes representing
the aggregate workflow.

The first step in a workflow is choosing an objective.

```kotlin
data object ObjectiveStep : Step
```

Specific questions are either stateless identifiers or classes with parameters.

```kotlin
data object SignInEmailAddressStep : Step

data class ChallengeCodeStep(
  val unverifiedEmailAddress: String,
) : Step
```

Steps can also encode feedback related to previous attempts at the step:

```kotlin
data class CreateComputerStep(
  val slugErrorMessage: String?,
  val slugSuggestions: List<String>?,
)
```

Steps can represent a workflow’s successful completion:

```kotlin
data class SignUpSuccessStep(
  val navigateTo: Url,
) : Step
```

Or a workflow that has failed.

```kotlin
data class TooManyAttemptsStep : Step
```

Both success and failure are terminal steps.

The tree structure supports aggregate steps.

```kotlin
data class SequenceStep(
  val current: Int,
  val steps: List<Step>
) : Step
```

Sequence is used to offer a series steps to the user.

For example, if we know that the step following `Username` is `Password`, we can show them both.
Aggregates may also be used to show paging information.

Sequences must have 2+ elements. The root of a step tree is either an `ObjectiveStep`
(single-node tree), or a `SequenceStep` with an `ObjectiveStep` as its first element.

```kotlin
data class ChoiceStep(
  val selected: Int,
  val steps: List<Step>
) : Step
```

Choice is used to offer alternatives. For example, when we need either a password or a passcode.

Choices must have 2+ elements.

```kotlin
data object SkipStep : Step
```

Skip is a special step that only occurs in `Choice`. If present it indicates that no choice is a
valid choice.

### Input

`Input` is a mirror of `Step` and describes the decision made by the user.

It can be a direct answer to a direct question.

```kotlin
data class EmailAddressInput(
  val emailAddress: String,
) : Input
```

```kotlin
data class ChallengeCodeInput(
  val challengeCode: String,
) : Input
```

Inputs have the same aggregates.

```kotlin
data class SequenceInput(
  val inputs: List<Input>
) : Input

data class ChoiceInput(
  val selected: Int,
  val input: Input
) : Step

data object SkipInput : Input
```

The number of elements in `SequenceInput` may be fewer than the number of elements in
`SequenceStep`. Only one choice is made, and its index is noted.

### WorkflowSnapshot

This is a server-provided snapshot of the workflow. It includes the input collected thus far, the
steps necessary to complete the objective, and an opaque blob of signed and encrypted context that
the server can use to keep track of a workflow that is held by the client.

```kotlin
data class WorkflowSnapshot(
  val step: Step,
  val input: Input,
  val encryptedStepContext: ByteString,
)
```

The `step` and `input` structures are parallel trees. They should have matching types in the same
positions.

### StepContext

Each time the server returns a `WorkflowSnapshot`, it embeds additional context as a message to its
future self to support processing future input.

For example, with email verification the 6-digit code itself can be sent to the client, signed and
encrypted, rather than being stored on-server. (The server still must persist data to enforce
[rate limits](./rate_limiting_permits.md), however).

The `StepContext` is a third parallel data structure. It bundles what we've sent and what we've
received so we don't need to trust the client.

Most of the tree can use default implementations.

```kotlin
data class DefaultStepContext(
  val step: Step,
  val input: Input?,
) : StepContext

data class SequenceStepContext(
  val steps: List<StepContext>,
) : StepContext

data class ChoiceStepContext(
  val selected: Int?,
  val steps: List<StepContext>,
) : StepContext
```

Only steps that leverage server information need custom `StepContext` subclass.

```kotlin
data class ChallengeCodeStepContext(
  val step: ChallengeCodeStep,
  val input: ChallengeCodeInput?,
  val code: String,
) : StepContext
```

### APIs

We use regular JSON-over-HTTP POST endpoints for workflows, but with a common request type and
response type.

```kotlin
data class AdvanceWorkflowRequest(
  val input: Input,
  val encryptedStepContext: ByteString?,
)

data class AdvanceWorkflowResponse(
  val workflowSnapshot: WorkflowSnapshot,
)
```

Initiate a new workflow by passing `AdvanceWorkflowRequest` with an `Objective` input and a null
`encryptedStepContext`.
