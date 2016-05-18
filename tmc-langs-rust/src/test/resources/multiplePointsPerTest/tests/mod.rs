#[macro_use]
extern crate points;

extern crate passing;

points! {
    #[points = {"10", "9"}]
    test it_shall_work {
        assert_eq!(9, passing::mul_xor_add(2, 3));
    }
}

