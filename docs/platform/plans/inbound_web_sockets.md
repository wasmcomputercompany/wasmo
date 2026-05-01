Inbound Web Sockets
===================

We can build web sockets, but the process serving a web socket call isn’t necessarily long-lived and
memory resident. Instead, we map web sockets into resources that can post messages and receive them.

With affinity, we can have our cake and eat it too: eject the app from memory without closing its
WebSockets!
