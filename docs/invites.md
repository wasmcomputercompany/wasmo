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
curl -v http://wasmo.localhost:8080/
```

Use the cookie to create an invite:

```
curl -v \
  --header 'Cookie: wasmo_session=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbiI6Imc4amJ4dmExdnJyYnJ6MXo5ZTRlM3ZjeHIiLCJpc3N1ZWRBdCI6IjIwMjYtMDMtMDNUMTU6NTM6MTUuNTIwNTU2WiJ9.rOBaZZYS1M_FnaDQt3vwzffEFXO4AJlg1-0DMlcVr5c=' \
  --data '{}' \
  http://wasmo.localhost:8080/create-invite
```
