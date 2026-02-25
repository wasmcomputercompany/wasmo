package com.wasmo.computers

import com.wasmo.app.db.WasmoDbService
import com.wasmo.identifiers.StripeCustomerId
import com.wasmo.payments.ComputerAllocationSnapshot
import com.wasmo.payments.PaymentsService
import com.wasmo.payments.SubscriptionSnapshot
import kotlin.time.Clock

/**
 * Call the Stripe API and sync a subscription to the database.
 */
class SubscriptionUpdater(
  private val clock: Clock,
  private val paymentsService: PaymentsService,
  private val wasmoDbService: WasmoDbService,
  private val computerSpecStore: ComputerSpecStore,
) {
  fun update(subscriptionId: String): SubscriptionSnapshot {
    val now = clock.now()
    val subscription = paymentsService.getSubscription(subscriptionId)

    val currentAllocation = ComputerAllocationSnapshot(
      activeStart = subscription.currentPeriodStart,
      activeEnd = subscription.currentPeriodEnd,
    )

    return wasmoDbService.transactionWithResult(noEnclosing = true) {
      val existingCustomer = wasmoDbService.stripeCustomerQueries
        .findStripeCustomerByStripeCustomerId(subscription.customer.id)
        .executeAsOneOrNull()

      val customerId: StripeCustomerId
      if (existingCustomer != null) {
        wasmoDbService.stripeCustomerQueries.updateStripeCustomer(
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
        customerId = wasmoDbService.stripeCustomerQueries.insertStripeCustomer(
          created_at = now,
          version = 1,
          stripe_customer_id = subscription.customer.id,
          name = subscription.customer.name,
          email = subscription.customer.email,
          country = subscription.customer.address.country,
          postal_code = subscription.customer.address.postalCode,
        ).executeAsOne()
      }

      val latestAllocation = wasmoDbService.computerAllocationQueries
        .findComputerAllocationByStripeSubscriptionId(
          stripe_subscription_id = subscriptionId,
          limit = 1L,
        ).executeAsOneOrNull()

      val computer = computerSpecStore.getOrCreateComputer(
        computerSpecToken = subscription.computerSpecToken,
      )

      if (latestAllocation == null) {
        // Create an allocation if we don't have one.
        wasmoDbService.computerAllocationQueries.insertComputerAllocation(
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
        wasmoDbService.computerAllocationQueries.truncateComputerAllocation(
          active_end = now,
          new_version = latestAllocation.version + 1,
          expected_version = latestAllocation.version,
          id = latestAllocation.id,
        )
        wasmoDbService.computerAllocationQueries.insertComputerAllocation(
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
