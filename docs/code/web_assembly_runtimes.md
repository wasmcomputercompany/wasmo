Web Assembly Runtimes
=====================

Raw notes on our Wasm runtime options.


Requirements
------------

In order to run Kotlin guest programs, we require these features from
[Wasm 3.0](https://webassembly.org/news/2025-09-17-wasm-3.0/):

 * [Wasm GC](https://github.com/WebAssembly/spec/blob/wasm-3.0/proposals/gc/Overview.md)
 * [Exception Handling](https://github.com/WebAssembly/spec/blob/wasm-3.0/proposals/exception-handling/Exceptions.md)
 * Dynamic code loading and unloading.
 * Runtime limits on execution and memory.
 * Strict sandboxing with host-provided imports.

Nice to have:

 * [Components](https://component-model.bytecodealliance.org/)
 * [Stack Switching](https://github.com/WebAssembly/stack-switching/blob/main/proposals/stack-switching/Explainer.md)


### [GraalWasm]

GraalVM is expecting to support Wasm GC in June 2026 in the 25.1 release. We've tried the early
access build and it crashes.

 * ([#13639](https://github.com/oracle/graal/issues/13639)).

The APIs to provide custom imports are difficult and sample code is sparse.


### [Chicory]

This is what we're currently using.


### [Endive]

This is likely to be the successor to Chicory. See the [Bytecode Alliance Announcement].


### [Chasm]

This is a small and friendly runtime.


Performance
-----------

[Minamoto79’s Benchmarks](https://github.com/minamoto79/webasm-java-integration-benchmark)


[Bytecode Alliance Announcement]: https://bytecodealliance.org/articles/endive-and-the-next-chapter-of-webassembly-on-the-jvm
[Chasm]: https://github.com/CharlieTap/chasm
[Chicory]: https://chicory.dev/
[Endive]: https://endive.run/
[GraalWasm]: https://www.graalvm.org/webassembly/
