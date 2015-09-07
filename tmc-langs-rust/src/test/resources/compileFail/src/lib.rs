
pub fn mul_xor_add(a: u64, b: u64) -> u64 {
    let x = a * b;
    (x ^ a) + (x ^ b);
}