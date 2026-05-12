# Module Graph

```mermaid
%%{
  init: {
    'theme': 'neutral'
  }
}%%

graph LR
  subgraph :apps
    :apps:samples["samples"]
  end
  subgraph :apps:journal
    :apps:journal:admin-web-app["admin-web-app"]
    :apps:journal:api["api"]
    :apps:journal:wasmo-app["wasmo-app"]
    :apps:journal:db["db"]
  end
  subgraph :os
    :os:api["api"]
    :os:framework["framework"]
    :os:logging["logging"]
    :os:routes["routes"]
    :os:cli["cli"]
    :os:json["json"]
    :os:catalog["catalog"]
  end
  subgraph :os:client
    :os:client:app["app"]
    :os:client:compose["compose"]
    :os:client:framework["framework"]
    :os:client:identifiers["identifiers"]
    :os:client:smartphoneframe["smartphoneframe"]
    :os:client:style_["style_"]
    :os:client:app-sandbox["app-sandbox"]
    :os:client:app-hosted["app-hosted"]
    :os:client:app-homelab["app-homelab"]
  end
  subgraph :os:client:passkeys
    :os:client:passkeys:api["api"]
    :os:client:passkeys:real["real"]
  end
  subgraph :os:server
    :os:server:identifiers["identifiers"]
    :os:server:db["db"]
    :os:server:wallpapers["wallpapers"]
    :os:server:okhttpclient["okhttpclient"]
    :os:server:vault["vault"]
  end
  subgraph :os:server:accounts
    :os:server:accounts:api["api"]
    :os:server:accounts:real["real"]
  end
  subgraph :os:server:calls
    :os:server:calls:wiring["wiring"]
    :os:server:calls:api["api"]
    :os:server:calls:real["real"]
  end
  subgraph :os:server:computers
    :os:server:computers:api["api"]
    :os:server:computers:real["real"]
  end
  subgraph :os:server:downloader
    :os:server:downloader:real["real"]
  end
  subgraph :os:server:emails
    :os:server:emails:real["real"]
    :os:server:emails:messages["messages"]
    :os:server:emails:attachments["attachments"]
  end
  subgraph :os:server:events
    :os:server:events:api["api"]
    :os:server:events:logging["logging"]
  end
  subgraph :os:server:installedapps
    :os:server:installedapps:api["api"]
    :os:server:installedapps:real["real"]
  end
  subgraph :os:server:jobs
    :os:server:jobs:api["api"]
    :os:server:jobs:absurd["absurd"]
  end
  subgraph :os:server:ktor
    :os:server:ktor:real["real"]
    :os:server:ktor:api["api"]
  end
  subgraph :os:server:objectstore
    :os:server:objectstore:fs["fs"]
    :os:server:objectstore:s3["s3"]
    :os:server:objectstore:api["api"]
  end
  subgraph :os:server:passkeys
    :os:server:passkeys:real["real"]
    :os:server:passkeys:api["api"]
  end
  subgraph :os:server:payments
    :os:server:payments:api["api"]
    :os:server:payments:stripe["stripe"]
  end
  subgraph :os:server:permits
    :os:server:permits:real["real"]
    :os:server:permits:api["api"]
  end
  subgraph :os:server:sendemail
    :os:server:sendemail:api["api"]
    :os:server:sendemail:postmark["postmark"]
  end
  subgraph :os:server:sql
    :os:server:sql:api["api"]
    :os:server:sql:real["real"]
    :os:server:sql:testing["testing"]
  end
  subgraph :os:server:wasm
    :os:server:wasm:api["api"]
    :os:server:wasm:jvm["jvm"]
  end
  subgraph :os:server:website
    :os:server:website:api["api"]
    :os:server:website:real["real"]
  end
  subgraph :platform
    :platform:testing["testing"]
    :platform:api["api"]
    :platform:packaging["packaging"]
  end
  subgraph :support
    :support:tokens["tokens"]
    :support:issues["issues"]
    :support:dom-tester["dom-tester"]
    :support:okio-html["okio-html"]
    :support:close-tracker["close-tracker"]
    :support:router["router"]
    :support:absurd["absurd"]
  end
  subgraph :wasmox
    :wasmox:wasmox-sql["wasmox-sql"]
    :wasmox:wasmox-sqldelight["wasmox-sqldelight"]
  end
  :os:server:passkeys:real --> :os:api
  :os:server:passkeys:real --> :os:framework
  :os:server:passkeys:real --> :os:server:accounts:api
  :os:server:passkeys:real --> :os:server:identifiers
  :os:server:passkeys:real --> :os:server:passkeys:api
  :os:server:passkeys:real --> :platform:testing
  :os:server:db --> :identifiers
  :os:server:db --> :os:api
  :os:server:db --> :os:framework
  :os:server:db --> :os:server:identifiers
  :os:server:db --> :os:server:passkeys:api
  :os:server:db --> :os:server:sql:api
  :os:server:db --> :platform:api
  :os:server:db --> :platform:packaging
  :os:server:db --> :support:tokens
  :os:server:db --> :wasmox:wasmox-sql
  :platform:testing --> :os:server:downloader:real
  :platform:testing --> :os:server:identifiers
  :platform:testing --> :os:server:objectstore:fs
  :platform:testing --> :os:server:sql:api
  :platform:testing --> :os:server:sql:real
  :platform:testing --> :platform:api
  :os:server:calls:wiring --> :os:framework
  :os:server:calls:wiring --> :os:server:accounts:api
  :os:server:calls:wiring --> :os:server:calls:api
  :os:server:calls:wiring --> :os:server:calls:real
  :os:server:calls:wiring --> :os:server:passkeys:api
  :os:server:calls:wiring --> :os:server:passkeys:real
  :os:server:jobs:api --> :os:api
  :os:server:jobs:api --> :os:server:identifiers
  :os:server:jobs:api --> :platform:api
  :os:server:jobs:api --> :wasmox:wasmox-sql
  :os:server:wasm:api --> :os:api
  :os:server:wasm:api --> :os:framework
  :os:server:wasm:api --> :os:server:accounts:api
  :os:server:wasm:api --> :os:server:db
  :os:server:wasm:api --> :os:server:identifiers
  :os:server:wasm:api --> :identifiers
  :os:server:wasm:api --> :platform:api
  :os:server:wasm:api --> :platform:packaging
  :platform:packaging --> :identifiers
  :platform:packaging --> :support:issues
  :os:server:payments:api --> :os:api
  :os:server:payments:api --> :identifiers
  :os:server:payments:api --> :platform:api
  :os:client:app --> :identifiers
  :os:client:app --> :os:api
  :os:client:app --> :os:client:compose
  :os:client:app --> :os:client:framework
  :os:client:app --> :os:client:identifiers
  :os:client:app --> :os:client:passkeys:api
  :os:client:app --> :os:client:passkeys:real
  :os:client:app --> :os:client:smartphoneframe
  :os:client:app --> :os:framework
  :os:client:app --> :os:logging
  :os:client:app --> :os:routes
  :os:client:app --> :support:tokens
  :os:client:app --> :support:dom-tester
  :os:client:app --> :os:client:style_
  :os:client:app-sandbox --> :os:client:app
  :os:cli --> :platform:packaging
  :os:cli --> :support:issues
  :os:server:website:api --> :os:api
  :os:server:website:api --> :os:framework
  :os:server:website:api --> :os:server:identifiers
  :os:server:website:api --> :platform:api
  :os:server:ktor:real --> :os:api
  :os:server:ktor:real --> :os:framework
  :os:server:ktor:real --> :os:logging
  :os:server:ktor:real --> :os:server:accounts:api
  :os:server:ktor:real --> :os:server:calls:wiring
  :os:server:ktor:real --> :os:server:identifiers
  :os:server:ktor:real --> :os:server:ktor:api
  :os:server:ktor:real --> :platform:api
  :os:server:ktor:real --> :support:issues
  :os:server:ktor:real --> :os:client:app
  :apps:samples --> :os:cli
  :os:server:identifiers --> :identifiers
  :os:server:identifiers --> :support:issues
  :os:server:calls:real --> :os:api
  :os:server:calls:real --> :os:framework
  :os:server:calls:real --> :os:routes
  :os:server:calls:real --> :os:server:accounts:api
  :os:server:calls:real --> :os:server:calls:api
  :os:server:calls:real --> :os:server:computers:api
  :os:server:calls:real --> :os:server:db
  :os:server:calls:real --> :os:server:identifiers
  :os:server:calls:real --> :os:server:passkeys:api
  :os:server:calls:real --> :os:server:sql:api
  :os:server:calls:real --> :identifiers
  :os:server:calls:real --> :platform:api
  :os:server:calls:real --> :wasmox:wasmox-sql
  :os:server:wallpapers --> :support:dom-tester
  :os:server:wallpapers --> :support:okio-html
  :os:server:sql:api --> :identifiers
  :os:server:sql:api --> :platform:api
  :os:server:sql:api --> :support:close-tracker
  :wasmox:wasmox-sqldelight --> :platform:api
  :os:server:permits:real --> :os:server:db
  :os:server:permits:real --> :os:server:identifiers
  :os:server:permits:real --> :os:server:permits:api
  :os:server:permits:real --> :os:server:sql:api
  :os:server:permits:real --> :platform:api
  :os:server:permits:real --> :wasmox:wasmox-sql
  :os:server:permits:real --> :platform:testing
  :os:server:computers:real --> :identifiers
  :os:server:computers:real --> :os:api
  :os:server:computers:real --> :os:framework
  :os:server:computers:real --> :os:logging
  :os:server:computers:real --> :os:server:accounts:api
  :os:server:computers:real --> :os:server:calls:api
  :os:server:computers:real --> :os:server:computers:api
  :os:server:computers:real --> :os:server:db
  :os:server:computers:real --> :os:server:downloader:real
  :os:server:computers:real --> :os:server:events:api
  :os:server:computers:real --> :os:server:identifiers
  :os:server:computers:real --> :os:server:installedapps:api
  :os:server:computers:real --> :os:server:jobs:api
  :os:server:computers:real --> :os:server:payments:api
  :os:server:computers:real --> :os:server:sql:api
  :os:server:computers:real --> :platform:api
  :os:server:computers:real --> :platform:packaging
  :os:server:computers:real --> :support:issues
  :os:server:computers:real --> :wasmox:wasmox-sql
  :os:server:computers:real --> :os:server:website:api
  :os:server:computers:real --> :os:server:website:real
  :os:server:computers:real --> :platform:testing
  :os:api --> :identifiers
  :os:api --> :os:framework
  :os:api --> :os:json
  :os:api --> :platform:packaging
  :os:api --> :support:tokens
  :os:server:wasm:jvm --> :identifiers
  :os:server:wasm:jvm --> :os:server:identifiers
  :os:server:wasm:jvm --> :os:server:wasm:api
  :os:server:wasm:jvm --> :platform:api
  :os:server:wasm:jvm --> :platform:packaging
  :os:server:emails:real --> :identifiers
  :os:server:emails:real --> :os:api
  :os:server:emails:real --> :os:framework
  :os:server:emails:real --> :os:server:db
  :os:server:emails:real --> :os:server:accounts:api
  :os:server:emails:real --> :os:server:calls:api
  :os:server:emails:real --> :os:server:emails:messages
  :os:server:emails:real --> :os:server:identifiers
  :os:server:emails:real --> :os:server:permits:api
  :os:server:emails:real --> :os:server:sendemail:api
  :os:server:emails:real --> :os:server:sql:api
  :os:server:emails:real --> :platform:api
  :os:server:emails:real --> :support:tokens
  :os:server:emails:real --> :wasmox:wasmox-sql
  :os:server:emails:real --> :platform:testing
  :os:server:passkeys:api --> :os:api
  :apps:journal:admin-web-app --> :apps:journal:api
  :apps:journal:admin-web-app --> :support:router
  :apps:journal:admin-web-app --> :support:tokens
  :apps:journal:admin-web-app --> :support:dom-tester
  :os:logging --> :support:issues
  :os:server:installedapps:api --> :os:api
  :os:server:installedapps:api --> :os:framework
  :os:server:installedapps:api --> :os:server:accounts:api
  :os:server:installedapps:api --> :os:server:db
  :os:server:installedapps:api --> :os:server:identifiers
  :os:server:installedapps:api --> :os:server:sql:api
  :os:server:installedapps:api --> :identifiers
  :os:server:installedapps:api --> :platform:api
  :os:server:installedapps:api --> :platform:packaging
  :os:server:installedapps:api --> :support:issues
  :os:server:installedapps:api --> :wasmox:wasmox-sql
  :os:framework --> :platform:api
  :os:framework --> :support:okio-html
  :wasmox:wasmox-sql --> :platform:api
  :os:server:emails:messages --> :os:framework
  :os:server:emails:messages --> :os:server:emails:attachments
  :os:server:emails:messages --> :os:server:sendemail:api
  :os:server:emails:messages --> :support:okio-html
  :os:server:emails:messages --> :support:tokens
  :os:server:emails:messages --> :support:dom-tester
  :os:server:accounts:real --> :os:api
  :os:server:accounts:real --> :os:framework
  :os:server:accounts:real --> :os:server:accounts:api
  :os:server:accounts:real --> :os:server:calls:api
  :os:server:accounts:real --> :os:server:db
  :os:server:accounts:real --> :os:server:emails:messages
  :os:server:accounts:real --> :os:server:identifiers
  :os:server:accounts:real --> :os:server:passkeys:api
  :os:server:accounts:real --> :os:server:sendemail:api
  :os:server:accounts:real --> :os:server:sql:api
  :os:server:accounts:real --> :os:server:website:api
  :os:server:accounts:real --> :os:server:website:real
  :os:server:accounts:real --> :platform:api
  :os:server:accounts:real --> :support:tokens
  :os:server:accounts:real --> :wasmox:wasmox-sql
  :os:server:accounts:real --> :platform:testing
  :os:server:events:logging --> :identifiers
  :os:server:events:logging --> :os:logging
  :os:server:events:logging --> :os:server:events:api
  :os:server:events:logging --> :os:server:identifiers
  :os:server:events:logging --> :os:server:installedapps:api
  :os:server:events:logging --> :support:issues
  :os:client:app-hosted --> :os:client:app
  :os:server:emails:attachments --> :os:server:sendemail:api
  :os:server:calls:api --> :os:api
  :os:server:calls:api --> :os:framework
  :os:server:calls:api --> :os:server:accounts:api
  :os:server:calls:api --> :os:server:db
  :os:server:calls:api --> :os:server:identifiers
  :os:server:calls:api --> :os:server:sql:api
  :os:server:calls:api --> :identifiers
  :os:server:calls:api --> :platform:api
  :os:server:calls:api --> :wasmox:wasmox-sql
  :os:server:website:real --> :os:api
  :os:server:website:real --> :os:framework
  :os:server:website:real --> :os:routes
  :os:server:website:real --> :os:server:accounts:api
  :os:server:website:real --> :os:server:calls:api
  :os:server:website:real --> :os:server:computers:api
  :os:server:website:real --> :os:server:db
  :os:server:website:real --> :os:server:identifiers
  :os:server:website:real --> :os:server:sql:api
  :os:server:website:real --> :os:server:wallpapers
  :os:server:website:real --> :os:server:website:api
  :os:server:website:real --> :identifiers
  :os:server:website:real --> :platform:api
  :os:server:website:real --> :support:okio-html
  :os:server:website:real --> :wasmox:wasmox-sql
  :os:server:permits:api --> :os:server:identifiers
  :os:server:permits:api --> :platform:api
  :os:server:payments:stripe --> :identifiers
  :os:server:payments:stripe --> :os:api
  :os:server:payments:stripe --> :os:catalog
  :os:server:payments:stripe --> :os:framework
  :os:server:payments:stripe --> :os:logging
  :os:server:payments:stripe --> :os:server:accounts:api
  :os:server:payments:stripe --> :os:server:calls:api
  :os:server:payments:stripe --> :os:server:computers:api
  :os:server:payments:stripe --> :os:server:db
  :os:server:payments:stripe --> :os:server:identifiers
  :os:server:payments:stripe --> :os:server:payments:api
  :os:server:payments:stripe --> :platform:api
  :os:server:payments:stripe --> :wasmox:wasmox-sql
  :os:server:payments:stripe --> :platform:testing
  :os:server:sql:testing --> :os:server:sql:api
  :os:server:sql:testing --> :os:server:sql:real
  :os:server:sql:testing --> :platform:api
  :os:server:sql:testing --> :support:close-tracker
  :os:server:sendemail:postmark --> :os:server:identifiers
  :os:server:sendemail:postmark --> :os:server:sendemail:api
  :os:server:sendemail:postmark --> :os:server:emails:attachments
  :os:server:sendemail:postmark --> :platform:testing
  :os:server:objectstore:s3 --> :os:server:identifiers
  :os:server:objectstore:s3 --> :os:server:objectstore:api
  :os:server:objectstore:s3 --> :platform:api
  :os:server:objectstore:s3 --> :platform:testing
  :os:server:okhttpclient --> :os:server:identifiers
  :os:server:okhttpclient --> :platform:api
  :os:server:events:api --> :identifiers
  :os:server:events:api --> :os:server:identifiers
  :os:server:events:api --> :support:issues
  :os:server:objectstore:api --> :os:server:identifiers
  :os:server:objectstore:api --> :platform:api
  :os:server:objectstore:api --> :os:server:objectstore:fs
  :os:server:objectstore:api --> :platform:testing
  :os:server:objectstore:api --> :support:tokens
  :os:client:passkeys:real --> :os:api
  :os:client:passkeys:real --> :os:client:identifiers
  :os:client:passkeys:real --> :os:client:passkeys:api
  :os:server:downloader:real --> :platform:api
  :os:server:jobs:absurd --> :os:server:events:api
  :os:server:jobs:absurd --> :os:server:identifiers
  :os:server:jobs:absurd --> :os:server:jobs:api
  :os:server:jobs:absurd --> :os:server:sql:api
  :os:server:jobs:absurd --> :platform:api
  :os:server:jobs:absurd --> :support:absurd
  :os:server:jobs:absurd --> :support:tokens
  :os:server:jobs:absurd --> :wasmox:wasmox-sql
  :os:server:jobs:absurd --> :platform:testing
  :os:server:installedapps:real --> :identifiers
  :os:server:installedapps:real --> :os:api
  :os:server:installedapps:real --> :os:framework
  :os:server:installedapps:real --> :os:server:accounts:api
  :os:server:installedapps:real --> :os:server:calls:api
  :os:server:installedapps:real --> :os:server:computers:api
  :os:server:installedapps:real --> :os:server:db
  :os:server:installedapps:real --> :os:server:downloader:real
  :os:server:installedapps:real --> :os:server:events:api
  :os:server:installedapps:real --> :os:server:identifiers
  :os:server:installedapps:real --> :os:server:installedapps:api
  :os:server:installedapps:real --> :os:server:jobs:api
  :os:server:installedapps:real --> :os:server:payments:api
  :os:server:installedapps:real --> :os:server:sql:api
  :os:server:installedapps:real --> :os:server:wasm:api
  :os:server:installedapps:real --> :platform:api
  :os:server:installedapps:real --> :platform:packaging
  :os:server:installedapps:real --> :support:close-tracker
  :os:server:installedapps:real --> :support:issues
  :os:server:installedapps:real --> :wasmox:wasmox-sql
  :os:server:installedapps:real --> :platform:testing
  :os:server:installedapps:real --> :os:server:website:api
  :os:server:installedapps:real --> :os:server:website:real
  :os:client:app-homelab --> :os:client:app
  :apps:journal:wasmo-app --> :os:cli
  :apps:journal:wasmo-app --> :apps:journal:admin-web-app
  :apps:journal:wasmo-app --> :apps:journal:api
  :apps:journal:wasmo-app --> :apps:journal:db
  :apps:journal:wasmo-app --> :support:tokens
  :apps:journal:wasmo-app --> :platform:api
  :apps:journal:wasmo-app --> :support:okio-html
  :apps:journal:wasmo-app --> :wasmox:wasmox-sqldelight
  :apps:journal:wasmo-app --> :platform:testing
  :apps:journal:wasmo-app --> :os:server:sql:api
  :apps:journal:wasmo-app --> :os:server:sql:real
  :os:client:passkeys:api --> :os:api
  :os:server:objectstore:fs --> :os:server:identifiers
  :os:server:objectstore:fs --> :os:server:objectstore:api
  :os:server:objectstore:fs --> :platform:api
  :os:server:sql:real --> :identifiers
  :os:server:sql:real --> :os:server:identifiers
  :os:server:sql:real --> :os:server:db
  :os:server:sql:real --> :os:server:sql:api
  :os:server:sql:real --> :platform:api
  :os:server:sql:real --> :support:close-tracker
  :os:server:sql:real --> :wasmox:wasmox-sql
  :os:server:sql:real --> :os:server:sql:testing
  :os:client:smartphoneframe --> :os:client:compose
  :os:client:smartphoneframe --> :support:dom-tester
  :os:server:accounts:api --> :os:api
  :os:server:accounts:api --> :os:framework
  :os:server:accounts:api --> :os:server:db
  :os:server:accounts:api --> :os:server:identifiers
  :os:server:accounts:api --> :os:server:sql:api
  :os:server:accounts:api --> :platform:api
  :os:server:accounts:api --> :wasmox:wasmox-sql
  :os:server:computers:api --> :identifiers
  :os:server:computers:api --> :os:api
  :os:server:computers:api --> :os:framework
  :os:server:computers:api --> :os:server:accounts:api
  :os:server:computers:api --> :os:server:db
  :os:server:computers:api --> :os:server:identifiers
  :os:server:computers:api --> :os:server:sql:api
  :os:server:computers:api --> :platform:packaging
  :os:server:computers:api --> :support:issues
  :os:server:computers:api --> :wasmox:wasmox-sql
  :os:server:vault --> :os:server:identifiers
  :os:routes --> :os:api
  :os:routes --> :os:framework
  :os:routes --> :identifiers
  :apps:journal:db --> :apps:journal:api
  :apps:journal:db --> :platform:api
  :apps:journal:db --> :wasmox:wasmox-sqldelight
```
