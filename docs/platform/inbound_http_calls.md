Inbound HTTP Calls
==================

This is how the application presents an HTML UI to browsers, and data API to mobile applications.
It may also be used for webhooks.

Inbound requests will include additional context about the caller:

- Original IP address
- Single-sign on identity

Inbound requests also strip Wasmo's cookies, so guest code can't see these.


Static Assets Serving
---------------------

Applications can embed static assets, and they'll be automatically served with appropriate caching
headers.


