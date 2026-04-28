package com.wasmo.computers

import com.wasmo.db.computers.findComputerAllocationByStripeSubscriptionId
import com.wasmo.db.computers.insertComputerAllocation
import com.wasmo.db.computers.truncateComputerAllocation
import com.wasmo.db.payments.stripe.findStripeCustomerByStripeCustomerId
import com.wasmo.db.payments.stripe.insertStripeCustomer
import com.wasmo.db.payments.stripe.updateStripeCustomer
import com.wasmo.identifiers.OsScope
import com.wasmo.identifiers.StripeCustomerId
import com.wasmo.payments.ComputerAllocationSnapshot
import com.wasmo.payments.PaymentsService
import com.wasmo.payments.SubscriptionSnapshot
import com.wasmo.sql.SqlTransaction
import com.wasmo.sql.transaction
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import wasmo.sql.SqlDatabase

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

    val currentAllocation = ComputerAllocationSnapshot(
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

      val latestAllocation = findComputerAllocationByStripeSubscriptionId(
        stripe_subscription_id = subscriptionId,
        limit = 1L,
      )

      val computer = computerStore.initializeFromSpec(
        computerSpecToken = subscription.computerSpecToken,
      )

      if (latestAllocation == null) {
        // Create an allocation if we don't have one.
        insertComputerAllocation(
          created_at = now,
          version = 1,
          stripe_customer_id = customerId,
          stripe_subscription_id = subscriptionId,
          computer_id = computer.id,
          active_start = currentAllocation.activeStart,
          active_end = currentAllocation.activeEnd,
        )
      } else if (latestAllocation.activeEnd != currentAllocation.activeEnd) {
        // If we have an allocation that's different, truncate it and create a replacement.
        truncateComputerAllocation(
          new_version = latestAllocation.version + 1,
          active_end = now,
          expected_version = latestAllocation.version,
          id = latestAllocation.id,
        )
        insertComputerAllocation(
          created_at = now,
          version = 1,
          stripe_customer_id = customerId,
          stripe_subscription_id = subscriptionId,
          computer_id = computer.id,
          active_start = now,
          active_end = currentAllocation.activeEnd,
        )
      }

      SubscriptionSnapshot(
        slug = computer.slug,
        currentAllocation = currentAllocation,
      )
    }
  }
}
