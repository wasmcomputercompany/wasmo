Resource Limits
===============

Sloppy apps will use 100% of our storage, CPU, memory, and bandwidth unless we actively limit them
from doing so.

This work is essential to support arbitrary apps for arbitrary users! But it's not urgent at all. We
can validate Wasmo as a product without implementing limits, because while we're exploring the
system we don't really care if our per-user compute costs are $5/month or $250/month. But when we
support self-sign-up we will care about this!


Storage
-------

Storage in S3 and PostgreSQL is a strict limit per-app, with a reservation API. Each app gets some
nominal amount of storage automatically, maybe 5 GiB, and can request more with the user's approval.
We could probably allocate more storage than we actually have, under the assumption that most apps
will use 100 MiB or whatever.


CPU, Memory, Bandwidth
----------------------

These resources are ephemeral: if we use up lots of CPU today, there's still lots of CPU to use
tomorrow. Our job is to guarantee that our supply of these resources is sufficient and correctly
distributed to the applications that use them.

We will take inspiration from Linux’s [Completely Fair Scheduler][CFS] for units and mechanisms to
track resource consumption.

We’ll also include APIs to convert resource use into money, so applications can document what their
resources cost the user.

If we can precisely track limits... we can attribute costs precisely. And that's gonna be how the
wasmo.com business is a business.


Implementing Limits
-------------------

One option is to use [Tigerbeetle] to track for resource use.

Or we could pin apps to host nodes, (affinity?) and maybe also pin entire computers to host nodes.
That way we can track and enforce ephemeral limits in memory, instead of with a distributed
database.


Resource Reservations
---------------------

We need a platform API to reserve storage. The app asks "can I have 100 GiB total?” and the
API returns a URL that gives the user an ‘Approve’ button, similar to our
[In App Purchases](in_app_purchases.md) system. The webpage might take a payment if the requested
amount exceeds the user's paid storage.

For symmetry apps can also decrease their reservation. This should probably just take effect
immediately without a page to confirm.


Activity Monitor
----------------

We need an OS-level Activity Monitor thing where users can see how their apps are using their
resources.

I expect an unscrupulous Solitaire app will be mining Bitcoin. When this happens we need a "report
this app" action! And I think our fix will be to block that app's access to the Internet.

We'll need to badge apps on the launcher when they're in the penalty box for mining Bitcoin!


[CFS]: https://en.wikipedia.org/wiki/Completely_Fair_Scheduler
[Tigerbeetle]: https://tigerbeetle.com/
