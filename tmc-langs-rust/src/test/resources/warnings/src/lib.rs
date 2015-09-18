
pub fn mul_xor_add(a: u64, b: u64) -> u64 {
    let x = a * b;
    xorAdd(x, a, b)
}

fn xorAdd(x: u64, a: u64, b: u64) -> u64 {
    let z = 0;
    (x ^ a) + (x ^ b)
}