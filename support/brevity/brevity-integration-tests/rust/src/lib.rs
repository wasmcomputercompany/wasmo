mod bindings {
    //! This module contains generated code for implementing
    //! the `adder` world in `wit/world.wit`.
    //!
    //! The `path` option is actually not required,
    //! as by default `wit_bindgen::generate` will look
    //! for a top-level `wit` directory and use the files
    //! (and interfaces/worlds) there-in.
    wit_bindgen::generate!({
        path: "../src/commonMain/wit/wasmo-testing.wit",

    });

  use super::WasmoTesting;
  export!(WasmoTesting);
}

struct WasmoTesting;

impl bindings::exports::wasmo::testing::calculator::Guest for WasmoTesting {
  fn multiply(a: i64, b: i64) -> i64 {
    a * b
  }
}

impl bindings::Guest for WasmoTesting {
  fn sum(a: i64, b: i64) -> i64 {
    a + b
  }
}
