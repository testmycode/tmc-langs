#![feature(plugin, custom_attribute)]
#![plugin(points)]

extern crate passing;

#[points = "10"]
#[test]
fn it_shall_work() {
    assert_eq!(9, passing::mul_xor_add(2, 3));
}
