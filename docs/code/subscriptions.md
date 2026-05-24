Subscriptions
=============

I'm going to start with [Stripe's Prebuilt Payment Form]. It supports subscriptions and captures
enough customer information with easy-enough setup.

I should start with the Stripe [Quickstart] doc.

Modeling Subscriptions
----------------------

A computer has these size parameters:

 - **ComputeCapacity**: how much CPU, RAM and network the computer is permitted to use. These are
   all ephemeral and use a [leaky bucket](https://en.wikipedia.org/wiki/Leaky_bucket) model for rate limiting.

 - **StorageCapacity**: how much data the computer may store. This is not ephemeral.

```kotlin
enum class ComputeCapacity {
  Standard2026,
}
```

We’ll create the `Computer` record in the database associated to an `AccountId`. Just because we
have a `Computer` does not mean that this computer has paid capacity allocated to it. We'll track
capacity allocation in the `ComputerSpec` table.

We’ll have a `SubscriptionPeriod` table that tracks a paid subscription for a computer's Capacity
for a given date range. Free computers exist for Homelab and will not require a subscription.

```kotlin
data class SubscriptionPeriod(
  val id: SubscriptionPeriodId,
  val computerId: ComputerId,
  val stripeSubscriptionId: String,
  val computeCapacity: ComputeCapacity,
  val storageCapacityGiB: Long,
  val from: Instant,
  val until: Instant,
  val paidDuration: Duration,
)
```

`SubscriptionPeriod` tracks _paid_ capacity for paid computers, but the _allocated_ capacity is
tracked in `ComputerSpec`. A computer can run if it is:
 - (a) free, **or**
 - (b) there is an active (subject to grace period) paid SubscriptionPeriod whose paid capacity is >= the allocated capacity.

We will create or update our `SubscriptionPeriod` objects when we receive a webhook from Stripe.

If a card is charged back we will immediately truncate the `until` date on the active
`SubscriptionPeriod`, if any. The `paidDuration` models how much of the time in `from..until` was
paid for, in the case that a payment was retroactively revoked.


Unpaid Subscriptions
--------------------

After payment lapses, we'll continue regular service for 14 days. You have to catch up.

We’ll delete the data on the minimum of

 - 60 days
 - The paid duration of the account, up to 365 days. So if your account is 90 days old, we won't
   delete your data for 90 days after payment lapsed.

We’ll send emails at these events:

 - 3 days after payment lapses, ‘Your Wasmo payment is overdue’
 - 11 days after payment lapses, ‘Your Wasmo will go offline in 3 days’
 - 57 days after payment lapses, ‘Your Wasmo will be deleted in 3 days’


Computers without Subscription
------------------------------

When loading a computer without a current `SubscriptionPeriod`, we'll determine whether it requires
payment (`paymentMethod="stripe" as opposed to paymentMethod="noneRequired"`) and, if so, do the
math to determine if it's in the grace period.

[Stripe's Prebuilt Payment Form]: https://checkout.stripe.dev/checkout
[Quickstart]: https://docs.stripe.com/checkout/embedded/quickstart?lang=java
