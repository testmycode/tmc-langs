extern crate passing;

#[cfg(points = "4")]
#[test]
fn it_shall_work() {
    assert_eq!(9, passing::mul_xor_add(2, 3));
}

#[test]
#[cfg(points = "7")]
fn it_shall_work2() {
    assert_eq!(5, passing::mul_xor_add(1, 4));
}