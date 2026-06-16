Local Development Web Assembly
==============================

Brevity requires some third-party tools to build WebAssembly.

Rust
----

The Rust folks recommend installing via [rustup] and not homebrew.

[rustup]: https://rust-lang.org/tools/install/


Web Assembly Target
-------------------

Once you've set up `rustup`, add a target to build `.wasm` files.

```bash
$ rustup target add wasm32-wasip2
```


wasm-tools
----------

You'll need [wasm-tools] to process generated `.wasm` files.

```bash
$ cargo binstall wasm-tools
```

[wasm-tools]: https://github.com/bytecodealliance/wasm-tools
