package com.wasmo.computers.free

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.CreateComputerSpecRequest
import com.wasmo.api.CreateComputerSpecResponse
import com.wasmo.computers.ComputerSpecStore
import com.wasmo.computers.ComputerStore
import com.wasmo.db.computers.insertComputerAllocation
import com.wasmo.framework.Response
import com.wasmo.framework.RpcAction
import com.wasmo.framework.Url
import com.wasmo.identifiers.StripeCustomerId
import com.wasmo.payments.ComputerAllocationSnapshot
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

@Inject
@ClassKey(FreeCreateComputerSpecRpc::class)
@ContributesIntoMap(CallScope::class, binding = binding<RpcAction<*, *>>())
class FreeCreateComputerSpecRpc(
  private val client: Client,
  private val wasmoDb: SqlDatabase,
  private val computerSpecStore: ComputerSpecStore,
  private val computerStore: ComputerStore,
  private val clock: Clock,
) : RpcAction<CreateComputerSpecRequest, CreateComputerSpecResponse.Success> {
  suspend fun create(
    request: CreateComputerSpecRequest,
  ): Response<CreateComputerSpecResponse.Success> {
    wasmoDb.transaction {
      computerSpecStore.insertIfAbsent(
        accountId = client.getOrCreateAccountId(),
        slug = request.slug,
        computerSpecToken = request.computerSpecToken,
      )
      val computer = computerStore.initializeFromSpec(request.computerSpecToken)
      val now = clock.now()

      // TODO: Replace these fake values with something better; make the fields nullable?
      val stripeCustomerId = StripeCustomerId(-1)
      val stripeSubscriptinId = ""
      val activeEnd = now.plus(36525.days) // approx. 100 years,

      insertComputerAllocation(
        created_at = now,
        version = 1,
        stripe_customer_id = stripeCustomerId,
        stripe_subscription_id = stripeSubscriptinId,
        computer_id = computer.id,
        active_start = now,
        active_end = activeEnd,
      )

    }
    return Response(
      body = CreateComputerSpecResponse.Success,
    )
  }

  override suspend operator fun invoke(
    request: CreateComputerSpecRequest,
    url: Url,
  ) = create(request)

}
