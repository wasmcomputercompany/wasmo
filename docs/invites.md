Invites
=======

We have invite_tickets in the database.

```
data class InviteTicket(
  val id: InviteTicketId,
  val createdAt: Instant,
  val createdBy: AccountId,
  val token: String,
  val claimedAt: Instant?,
  val claimedBy: AccountId?,
)
```

Accounts that have an `invite_ticket` have capabilities that other accounts do not.

Creating an Invite
------------------

Get a cookie:

```
curl -v  http://localhost:8080/
```

Use the cookie to create an invite:

```
curl -v \
  --header 'Cookie: session=...' \
  --data '{}' \
  http://localhost:8080/create-invite
```
