extern crate failing;

#[test]
fn it_shall_work() {
    assert_eq!(9, failing::mul_xor_add(2, 3));
}