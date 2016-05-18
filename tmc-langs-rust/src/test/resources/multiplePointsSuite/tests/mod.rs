#[macro_use]
extern crate points;

extern crate passing;

points! {
    #[points = {"4"}]
    suite exercise1 {
        #[points = {"6"}]
        test it_shall_work {
            assert_eq!(9, passing::mul_xor_add(2, 3));
        }

        #[points = {"7"}]
        test it_shall_work2 {
            assert_eq!(5, passing::mul_xor_add(1, 4));
        }
    }
}
