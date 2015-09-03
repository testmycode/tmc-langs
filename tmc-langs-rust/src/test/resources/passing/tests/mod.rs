extern crate passing_suite;
use passing_suite::mul_xor_add;

#[test]
fn it_shall_work() {
	assert_eq!(9, mul_xor_add(2, 3));
}