package com.wasmo.emails

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.ConfirmEmailAddressRequest
import com.wasmo.api.ConfirmEmailAddressResponse
import com.wasmo.api.ConfirmEmailAddressResponse.Decision
import com.wasmo.calls.CallDataService
import com.wasmo.db.emails.insertLinkedEmailAddress
import com.wasmo.db.emails.selectLinkedEmailAddressOrNull
import com.wasmo.framework.Response
import com.wasmo.identifiers.EmailAddressLinkPermitType
import com.wasmo.permits.PermitService
import com.wasmo.permits.RateLimit
import com.wasmo.sql.SqlTransaction
import com.wasmo.sql.transaction
import com.wasmo.support.tokens.toChallengeCodeOrNull
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import wasmo.sql.SqlDatabase

val ChallengeAttemptRateLimit = RateLimit(
  count = 3,
  duration = 24.hours,
)

@Inject
@SingleIn(CallScope::class)
class ConfirmEmailAddressAction(
  private val clock: Clock,
  private val client: Client,
  private val challengeTokenChecker: ChallengeTokenChecker,
  private val callDataService: CallDataService,
  private val permitService: PermitService,
  private val wasmDb: SqlDatabase,
) {
  suspend fun confirm(
    request: ConfirmEmailAddressRequest,
  ): Response<ConfirmEmailAddressResponse> {
    return wasmDb.transaction {
      val decision = decideAndAct(request)
      val account = when (decision) {
        Decision.LinkedNew, Decision.LinkedExisting -> callDataService.accountSnapshot()
        else -> null
      }

      Response(
        body = ConfirmEmailAddressResponse(
          decision = decision,
          account = account,
        ),
      )
    }
  }

  context(sqlTransaction: SqlTransaction)
  suspend fun decideAndAct(
    request: ConfirmEmailAddressRequest,
  ): Decision {
    val now = clock.now()

    val accountId = client.getAccountIdOrNull()
      ?: return Decision.BadRequest

    val challengeCode = request.challengeCode.toChallengeCodeOrNull()
      ?: return Decision.BadRequest

    val permitAcquired = permitService.tryAcquire(
      now = now,
      type = EmailAddressLinkPermitType,
      value = request.unverifiedEmailAddress,
      rateLimit = ChallengeAttemptRateLimit,
    )
    if (!permitAcquired) return Decision.TooManyAttempts

    val challengeCodeAccepted = challengeTokenChecker.check(
      accountId = accountId,
      emailAddress = request.unverifiedEmailAddress,
      challengeCode = challengeCode,
      challengeToken = request.challengeToken,
    )
    if (!challengeCodeAccepted) return Decision.WrongChallengeCode

    // Upon a successful challenge we release the permit.
    permitService.tryAcquire(
      now = now,
      type = EmailAddressLinkPermitType,
      value = request.unverifiedEmailAddress,
      count = -1L,
      rateLimit = ChallengeAttemptRateLimit,
    )

    val linkedEmailAddress = selectLinkedEmailAddressOrNull(request.unverifiedEmailAddress)

    return when {
      linkedEmailAddress == null -> {
        insertLinkedEmailAddress(
          createdAt = now,
          accountId = accountId,
          emailAddress = request.unverifiedEmailAddress,
          active = true,
        )
        Decision.LinkedNew
      }

      else -> {
        client.signIn(
          source = accountId,
          target = linkedEmailAddress.accountId,
        )
        Decision.LinkedExisting
      }
    }
  }
}
