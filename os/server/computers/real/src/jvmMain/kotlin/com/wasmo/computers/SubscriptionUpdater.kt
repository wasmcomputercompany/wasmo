package com.wasmo.computers

import com.wasmo.app.db.SqlTransaction
import com.wasmo.app.db.WasmoDb
import com.wasmo.app.db.findComputerAllocationByStripeSubscriptionId
import com.wasmo.app.db.findStripeCustomerByStripeCustomerId
import com.wasmo.app.db.insertComputerAllocation
import com.wasmo.app.db.insertStripeCustomer
import com.wasmo.app.db.truncateComputerAllocation
import com.wasmo.app.db.updateStripeCustomer
import com.wasmo.identifiers.OsScope
import com.wasmo.identifiers.StripeCustomerId
import com.wasmo.payments.ComputerAllocationSnapshot
import com.wasmo.payments.PaymentsService
import com.wasmo.payments.SubscriptionSnapshot
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock

/**
 * Call the Stripe API and sync a subscription to the database.
 */
@Inject
@SingleIn(OsScope::class)
class SubscriptionUpdater(
  private val clock: Clock,
  private val paymentsService: PaymentsService,
  private val wasmoDb: WasmoDb,
  private val computerStore: ComputerStore,
) {
  suspend fun update(subscriptionId: String): SubscriptionSnapshot {
    val now = clock.now()
    val subscription = paymentsService.getSubscription(subscriptionId)

    val currentAllocation = ComputerAllocationSnapshot(
      activeStart = subscription.currentPeriodStart,
      activeEnd = subscription.currentPeriodEnd,
    )

    return wasmoDb.transactionWithResult(noEnclosing = true) {
      val existingCustomer = contextOf<SqlTransaction>()
        .findStripeCustomerByStripeCustomerId(subscription.customer.id)

      val customerId: StripeCustomerId
      if (existingCustomer != null) {
        contextOf<SqlTransaction>().updateStripeCustomer(
          new_version = existingCustomer.version + 1,
          name = subscription.customer.name,
          email = subscription.customer.email,
          country = subscription.customer.address.country,
          postal_code = subscription.customer.address.postalCode,
          expected_version = existingCustomer.version,
          id = existingCustomer.id,
        )
        customerId = existingCustomer.id
      } else {
        customerId = contextOf<SqlTransaction>().insertStripeCustomer(
          created_at = now,
          version = 1,
          stripe_customer_id = subscription.customer.id,
          name = subscription.customer.name,
          email = subscription.customer.email,
          country = subscription.customer.address.country,
          postal_code = subscription.customer.address.postalCode,
        )
      }

      val latestAllocation = contextOf<SqlTransaction>()
        .findComputerAllocationByStripeSubscriptionId(
          stripe_subscription_id = subscriptionId,
          limit = 1L,
        )

      val computer = computerStore.initializeFromSpec(
        computerSpecToken = subscription.computerSpecToken,
      )

      if (latestAllocation == null) {
        // Create an allocation if we don't have one.
        contextOf<SqlTransaction>().insertComputerAllocation(
          created_at = now,
          version = 1,
          stripe_customer_id = customerId,
          stripe_subscription_id = subscriptionId,
          computer_id = computer.id,
          active_start = currentAllocation.activeStart,
          active_end = currentAllocation.activeEnd,
        )
      } else if (latestAllocation.active_end != currentAllocation.activeEnd) {
        // If we have an allocation that's different, truncate it and create a replacement.
        contextOf<SqlTransaction>().truncateComputerAllocation(
          active_end = now,
          new_version = latestAllocation.version + 1,
          expected_version = latestAllocation.version,
          id = latestAllocation.id,
        )
        contextOf<SqlTransaction>().insertComputerAllocation(
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
