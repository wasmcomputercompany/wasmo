Invites
=======

We have invite_tickets in the database.

```
data class InviteTicket(
  val id: InviteTicketId,
  val createdAt: Instant,
  val token: String,
  val claimedBy: AccountId?,
  val claimedAt: Instant?,
)
```

Accounts that have an `invite_ticket` have capabilities that other accounts do not.
