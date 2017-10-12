library('testthat')

points_for_all_tests(c("r4"))

help_A <- matrix(c(3, 5, 6, 1/2, sqrt(5), 16, 0, 2, 0),nrow = 3, ncol = 3, byrow = TRUE)

test("Exercise 1 is correct", c("r4.1"), {
    expect_equal(a1, 14)
    expect_equal(b1, 70)
    expect_equal(c1, 343000)
    expect_true(abs(e1-1096.63) < 0.1)
    expect_true(abs(f1 - 0.7920019) < 0.1)
    expect_true(abs(res1 - 344167.4) < 0.1)
})

test("Exercise 2 is correct", c("r4.2"), {
    expect_equal(a2, 2)
})

test("Exercise 3 is correct", c("r4.3", "r4.4"), {
    expect_equal(a3, 9)
})

test("Exercise 4 is correct", c("r4.5"), {
    expect_equal(v4_1, c(20, 5, -2, 3, 47))
    expect_equal(v4_2, c(0:20)*5)
    expect_equal(v4_3, c(c(20, 5, -2, 3, 47), seq(0, 100, 5)))
    expect_equal(v4_4, c(20, 5, 47, 5, 10, 15, 20, 25, 30, 35, 40, 45))
    expect_equal(v4_5, c(20, 10, 20, 30, 40))
})

test("Exercise 5 is correct", c("r4.6"), {
    t1 <- rep(0, 1, 50)
    t1[c(F, T)] <- 2
    expect_equal(v5_1, t1)
    expect_equal(sum5_1, 50)
    expect_equal(sum5_2, 80)
})

test("Exercise 6 is correct", c("r4.7"), {
    expect_equal(A, matrix(c(3, 5, 6, 1/2, sqrt(5), 16, 0, 2, 0),nrow = 3, ncol = 3, byrow = TRUE))
})

test("Exercise 7 is correct", c("r4.8"), {
  expect_equal(A, help_A)
})

test("Exercise 8 is correct", c("r4.9"), {
  expect_equal(B, c(1, 1, 0)%*%solve(help_A))
})

test("Exercise 9 is correct", c("r4.10"), {
  expect_equal(I_3, diag(c(1, 1, 1)))
  expect_equal(A_8, help_A)
})

test("Exercise 10 is correct", c("r4.11"), {
  expect_false(is_eq_matrix)
})

test("Exercise 11 is correct", c("r4.12"), {
  expect_true(number10 < 10)

  expect_true(dim(E_10)[1] == 4)
  expect_true(dim(E_10)[2] == 3)
})

test("Exercise 12 is correct", c("r4.13"), {
  M <- matrix(1:100, ncol=2)
  M[c(F, T)] <- NA
  expect_equal(C_11, M)
})

test("Exercise 13 is correct", c("r4.14"), {
  M <- matrix(1:100, ncol=2)
  M[c(F, T)] <- 0
  expect_equal(C_12, M)
})
