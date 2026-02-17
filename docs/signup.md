Sign Up
=======

Right now sign up is somewhat quick-and-dirty. We collect a bunch of information optimistically,
and do not resume the flow if it fails.

We need to implement proper web navigation (back & forward).

```kotlin
data class SignUpState(
  val unverifiedEmailAddress: String?,
  val passkey: ByteString?,
  val paymentInstrument: PaymentInstrument?,
  val wasmoName: String,
)
```

Linking an Email Address
------------------------

Accounts have at most one unverified email address at a time.

Attempting to add an unverified email address cancels all previous ones.

```
data class LinkEmailAddressRequest(
  val unverifiedEmailAddress: String,
)
```

We have APIs for linking an email address and confirming a link with a code.

```
data class ConfirmEmailAddressRequest(
  val emailAddress: String,
  val challengeCode: String,
)
```

Registering a Passkey
---------------------

Once we accept the customer's email address, we prompt to confirm our passkey.

```
data class LookupPasskeyRequest(
  val unverifiedEmailAddress: String,
)
```

We have an API to register a passkey. Once we have a passkey we don't prompt to register another.

```
data class RegisterPasskeyRequest(
  val passkey: ByteString,
)
```

Payment
-------

We have an API to accept payment and store the payment card.

```
data class FundAccountRequest(
  val canadianCreditCard: CanadianCreditCard,
)
```

Create Wasmo
------------

```
data class CreateWasmoRequest(
  val name: String,
)
```

