Rust integration tests
======================

Confirm Brevity's generated APIs interop with Rust.

Build
-----

```bash
$ cargo build \
  --target=wasm32-wasip2 \
  --release
$ wasm-tools component unbundle \
  --module-dir target/unbundled/ \
  --output target/unbundled/component.wasm \
  ./target/wasm32-wasip2/release/wasmo_testing.wasm
```

Debugging
---------

```bash
$ cargo install cargo-expand
```

```bash
$ cargo expand
```
