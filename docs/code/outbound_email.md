Outbound Email
==============

We're starting with [Postmark] as our outbound email service.

We're using their [Email API].

```
curl "https://api.postmarkapp.com/email" \
  -X POST \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -H "X-Postmark-Server-Token: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" \
  -d '{
        "From": "jesse@wasmo.com",
        "To": "jesse@wasmo.com",
        "Subject": "Hello from Postmark",
        "HtmlBody": "<strong>Hello</strong> dear Postmark user.",
        "MessageStream": "outbound"
      }'
```

[Postmark]: https://postmarkapp.com/
[Email API]: https://postmarkapp.com/developer/api/email-api
