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
  --header 'Cookie: session=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbiI6IjNwemQ0d3EwYWs0d3I1ZXQwaGF4YjZ0ankiLCJpc3N1ZWRBdCI6IjIwMjYtMDItMjRUMTg6NTQ6MTMuNTAzNTQ2WiJ9.RuTzcNdL06oxp9xSV4U2snrM070fb1wGvelRD0_P8D8=' \
  --data '{}' \
  http://localhost:8080/create-invite
```
