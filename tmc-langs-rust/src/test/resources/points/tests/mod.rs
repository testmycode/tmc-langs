extern crate passing;

#[cfg(points = "10")]
#[test]
fn it_shall_work() {
    assert_eq!(9, passing::mul_xor_add(2, 3));
}