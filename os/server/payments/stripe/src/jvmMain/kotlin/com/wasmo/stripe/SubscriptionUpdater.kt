package com.wasmo.stripe

import com.wasmo.computers.ComputerStore
import com.wasmo.db.computers.findSubscriptionPeriodByStripeSubscriptionId
import com.wasmo.db.computers.insertSubscriptionPeriod
import com.wasmo.db.computers.truncateSubscriptionPeriod
import com.wasmo.db.payments.stripe.findStripeCustomerByStripeCustomerId
import com.wasmo.db.payments.stripe.insertStripeCustomer
import com.wasmo.db.payments.stripe.updateStripeCustomer
import com.wasmo.identifiers.OsScope
import com.wasmo.identifiers.StripeCustomerId
import com.wasmo.payments.SubscriptionPeriodSnapshot
import com.wasmo.payments.PaymentsService
import com.wasmo.payments.SubscriptionSnapshot
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import wasmo.sql.SqlDatabase
import wasmox.sql.SqlTransaction
import wasmox.sql.transaction

/**
 * Call the Stripe API and sync a subscription to the database.
 */
@Inject
@SingleIn(OsScope::class)
class SubscriptionUpdater(
  private val clock: Clock,
  private val paymentsService: PaymentsService,
  private val wasmoDb: SqlDatabase,
  private val computerStore: ComputerStore,
) {
  suspend fun update(subscriptionId: String): SubscriptionSnapshot {
    val now = clock.now()
    val subscription = paymentsService.getSubscription(subscriptionId)

    val currentSubscriptionPeriod = SubscriptionPeriodSnapshot(
      activeStart = subscription.currentPeriodStart,
      activeEnd = subscription.currentPeriodEnd,
    )

    return wasmoDb.transaction {
      val existingCustomer = with(contextOf<SqlTransaction>()) {
        findStripeCustomerByStripeCustomerId(subscription.customer.id)
      }

      val customerId: StripeCustomerId
      if (existingCustomer != null) {
        updateStripeCustomer(
          newVersion = existingCustomer.version + 1,
          name = subscription.customer.name,
          email = subscription.customer.email,
          country = subscription.customer.address.country,
          postalCode = subscription.customer.address.postalCode,
          expectedVersion = existingCustomer.version,
          id = existingCustomer.id,
        )
        customerId = existingCustomer.id
      } else {
        customerId = insertStripeCustomer(
          createdAt = now,
          version = 1,
          stripeCustomerId = subscription.customer.id,
          name = subscription.customer.name,
          email = subscription.customer.email,
          country = subscription.customer.address.country,
          postalCode = subscription.customer.address.postalCode,
        )
      }

      val latestSubscriptionPeriod = findSubscriptionPeriodByStripeSubscriptionId(
        stripe_subscription_id = subscriptionId,
        limit = 1L,
      )

      val computer = computerStore.initializeFromSpec(
        computerSpecToken = subscription.computerSpecToken,
      )

      if (latestSubscriptionPeriod == null) {
        // Create a SubscriptionPeriod if we don't have one.
        insertSubscriptionPeriod(
          created_at = now,
          version = 1,
          stripe_customer_id = customerId,
          stripe_subscription_id = subscriptionId,
          computer_id = computer.id,
          active_start = currentSubscriptionPeriod.activeStart,
          active_end = currentSubscriptionPeriod.activeEnd,
        )
      } else if (latestSubscriptionPeriod.activeEnd != currentSubscriptionPeriod.activeEnd) {
        // If we have a SubscriptionPeriod that's different, truncate it and create a replacement.
        truncateSubscriptionPeriod(
          new_version = latestSubscriptionPeriod.version + 1,
          active_end = now,
          expected_version = latestSubscriptionPeriod.version,
          id = latestSubscriptionPeriod.id,
        )
        insertSubscriptionPeriod(
          created_at = now,
          version = 1,
          stripe_customer_id = customerId,
          stripe_subscription_id = subscriptionId,
          computer_id = computer.id,
          active_start = now,
          active_end = currentSubscriptionPeriod.activeEnd,
        )
      }

      SubscriptionSnapshot(
        slug = computer.slug,
        currentSubscriptionPeriod = currentSubscriptionPeriod,
      )
    }
  }
}
