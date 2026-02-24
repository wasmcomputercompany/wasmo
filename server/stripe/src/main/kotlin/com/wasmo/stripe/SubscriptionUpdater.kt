package com.wasmo.stripe

import com.stripe.param.SubscriptionRetrieveParams
import com.stripe.service.SubscriptionService
import com.wasmo.app.db.WasmoDbService
import com.wasmo.common.catalog.Catalog
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.StripeCustomerId
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Call the Stripe API and sync a subscription to the database.
 */
class SubscriptionUpdater(
  private val clock: Clock,
  private val subscriptionService: SubscriptionService,
  private val catalog: Catalog,
  private val wasmoDbService: WasmoDbService,
) {
  fun update(subscriptionId: String) {
    val now = clock.now()
    val subscription = subscriptionService.retrieve(
      subscriptionId,
      SubscriptionRetrieveParams.Builder()
        .addExpand("customer")
        .build(),
    )

    val item = subscription.items.data.single()
    require(item.price.id == catalog.wasmoStandard.priceId)
    require(item.quantity == 1L)
    val activeStart = Instant.fromEpochSeconds(item.currentPeriodStart)
    val activeEnd = Instant.fromEpochSeconds(item.currentPeriodEnd)

    wasmoDbService.transactionWithResult(noEnclosing = true) {
      val customerObject = subscription.customerObject
      val existingCustomer = wasmoDbService.stripeCustomerQueries
        .findStripeCustomerByStripeCustomerId(subscription.customer)
        .executeAsOneOrNull()

      val customerId: StripeCustomerId
      if (existingCustomer != null) {
        wasmoDbService.stripeCustomerQueries.updateStripeCustomer(
          new_version = existingCustomer.version + 1,
          name = customerObject.name,
          email = customerObject.email,
          country = customerObject.address.country,
          postal_code = customerObject.address.postalCode,
          expected_version = existingCustomer.version,
          id = existingCustomer.id,
        )
        customerId = existingCustomer.id
      } else {
        customerId = wasmoDbService.stripeCustomerQueries.insertStripeCustomer(
          created_at = now,
          version = 1,
          stripe_customer_id = subscription.customer,
          name = customerObject.name,
          email = customerObject.email,
          country = customerObject.address.country,
          postal_code = customerObject.address.postalCode,
        ).executeAsOne()
      }

      val latestAllocation = wasmoDbService.computerAllocationQueries
        .findComputerAllocationByStripeSubscriptionId(
          stripe_subscription_id = subscriptionId,
          limit = 1L,
        ).executeAsOneOrNull()

      val computerId = ComputerId(1L)

      if (latestAllocation == null) {
        // Create an allocation if we don't have one.
        wasmoDbService.computerAllocationQueries.insertComputerAllocation(
          created_at = now,
          version = 1,
          stripe_customer_id = customerId,
          stripe_subscription_id = subscriptionId,
          computer_id = computerId,
          active_start = activeStart,
          active_end = activeEnd,
        )
      } else if (latestAllocation.active_end != activeEnd) {
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
          computer_id = computerId,
          active_start = now,
          active_end = activeEnd,
        )
      }
    }
  }
}
