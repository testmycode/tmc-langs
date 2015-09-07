extern crate passing;

#[test]
fn it_shall_work() {
    assert_eq!(9, passing::mul_xor_add(2, 3));
}

#[test]
fn it_shall_work2() {
    assert_eq!(5, passing::mul_xor_add(1, 4));
}