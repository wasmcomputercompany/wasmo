Host Migrations
---------------

We need to build a mechanism to move house: a user moves from say, wasmo.com to Homelab Wasmo or
Amazon EWS (Elastic Wasmo Service!) or vice versa.

This should be an online process, where the old and new host coordinate the move on the user’s
behalf.

```mermaid
sequenceDiagram
  participant Jesse
  Jesse->>wasmo.com: ‘I would like to move jesse99’
  wasmo.com->>Jesse: ‘Please confirm with your passkey’
  Jesse->>wasmo.com: ‘Here you go.’
  wasmo.com->>Jesse: ‘Your secret transfer code is<br>jesse99.8a9b7c6d.amVzc2U5OS53YXNtby5jb20’
  Jesse->>ews.aws.com: ‘My secret transfer code is<br>jesse99.8a9b7c6d.amVzc2U5OS53YXNtby5jb20’
  ews.aws.com->>wasmo.com: ‘Tell me about<br>jesse99.8a9b7c6d.amVzc2U5OS53YXNtby5jb20’
  wasmo.com->>ews.aws.com: ‘sdk v1, 201 GB of data, 300 CPU’
  ews.aws.com->>Jesse: ‘Please pay $10 for your first month’
  Jesse->>ews.aws.com: ‘Here you go’
  ews.aws.com->>Jesse: ‘Nice. Transfer will take 8-10 minutes’
  ews.aws.com->>wasmo.com: ‘Send me a block from <br>jesse99.8a9b7c6d.amVzc2U5OS53YXNtby5jb20’
  wasmo.com->>ews.aws.com: ‘Here’s block 0...’
  ews.aws.com->>wasmo.com: ‘Send me another block from <br>jesse99.8a9b7c6d.amVzc2U5OS53YXNtby5jb20’
  wasmo.com->>ews.aws.com: ‘Here’s block 1...’
  ews.aws.com->>wasmo.com: ‘Send me another block from <br>jesse99.8a9b7c6d.amVzc2U5OS53YXNtby5jb20’
  wasmo.com->>ews.aws.com: ‘Here’s block 2...’
  ews.aws.com->>wasmo.com: ‘Send me another block from <br>jesse99.8a9b7c6d.amVzc2U5OS53YXNtby5jb20’
  wasmo.com->>ews.aws.com: ‘You are up-to-date.’
  ews.aws.com->>wasmo.com: ‘Complete the transfer of <br>jesse99.8a9b7c6d.amVzc2U5OS53YXNtby5jb20<br>to jwilson.ews.aws.com’
  wasmo.com->>ews.aws.com: ‘I have halted all processes and added redirects.’
  ews.aws.com->>wasmo.com: ‘Send me another block from <br>jesse99.8a9b7c6d.amVzc2U5OS53YXNtby5jb20’
  wasmo.com->>ews.aws.com: ‘Here’s block 3...’
  ews.aws.com->>wasmo.com: ‘Send me another block from <br>jesse99.8a9b7c6d.amVzc2U5OS53YXNtby5jb20’
  wasmo.com->>ews.aws.com: ‘You are up-to-date.’
  ews.aws.com->>Jesse: ‘Transfer complete.’
```

Redirects
---------

We should do HTTP redirects for a grace period like 3 months. Perhaps users can purchase redirects
at some low price, like $10 per year.

It must be redirects only. We can’t point our DNS names at the recipient’s host because it would
leak the source’s secret cookies to the target server.

