mod xor_adder;

pub fn mul_xor_add(a: u64, b: u64) -> u64 {
    let z = 0;
    let x = a * b;
    xor_adder::xorAdd(x, a, b)
}