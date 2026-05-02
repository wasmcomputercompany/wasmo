Wasmo OS Artifacts
==================

We build different artifacts for different use-cases.


Artifacts
---------

### Homelab Wasmo OS

This is for tech nerds and people who want complete custody of their Wasmo computers.

It is distributed as a self-contained Docker image.

### Hosted Wasmo OS

This powers `wasmo.com`, our hosted service for end-users. We expect that other organizations will
use this artifact to power their own hosted services. Many different hosts are good for the
ecosystem, even if it means we don’t keep all customers to ourselves.

### Sandbox Wasmo OS (for developers)

This powers `wasmo.dev`, our sandbox environment for application developers. They can use the
environment to integration test their applications before releasing them.

This environment supports the [In-App Purchases System], but with fake money only.

Data on this service is ephemeral: all computers are automatically deleted 12 hours after they’re
created.

### Wasmo SDK (for developers)

This is a cut down Wasmo OS for application developers to run locally while building applications.
It attempts to be real-enough to be representative of Hosted and Homelab Wasmo OS. It also offers
hooks for developers to see everything that’s going on in their apps.


Feature Matrix
--------------

|                  | Where it runs            | Payments                | Object Store        | Postgresql  | Outbound Email |
|:-----------------|:-------------------------|:------------------------|:--------------------|:------------|:---------------|
| Homelab Wasmo OS | home server              | Wasmo Shop API + Stripe | Docker volume mount | Docker      | Optional SMTP  |
| Hosted Wasmo OS  | our datacenter           | direct to Stripe        | S3 (Backblaze B2)   | PlanetScale | Postmark       |
| Sandbox Wasmo OS | our datacenter           | persistent fake         | S3 (Backblaze B2)   | PlanetScale | Postmark       |
| Wasmo SDK        | app developer’s computer | persistent fake         | local file system   | localhost   | Optional SMTP  |

Wasmo OS Development
--------------------

When developing Wasmo OS itself it’s inconvenient to stand up accounts for integrations with 3rd
party services like B2, Stripe, and Postmark. We offer development builds for all of the above that
fake these integrations.

For example, local dev builds of Hosted Wasmo OS will use Stripe if API keys are present, and fall
back to a persistent fake otherwise.

