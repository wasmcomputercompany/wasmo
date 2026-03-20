In-App Purchases
================

Installed applications can request in-app purchases that must be approved by the user interactively
through a OAuth-like flow.

This may be used to unlock features in the application, such as a 'Supporter' badge in Immich.

When payment is received we will optionally notify an external endpoint owned by the application's
vendor. That endpoint should acknowledge receipt and return a signed token that can be validated by
the installed application. This entire scheme prevents self-hosted applications from spoofing
in-app purchases.



