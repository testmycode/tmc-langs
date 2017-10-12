library('testthat')

points_for_all_tests(c("r2"))

test("Matrix transpose with [[1,2]] works", c("r2.1"), {
  A<- matrix(c(1,2),nrow=1)
  expect_equal(transpose_matrix(A),matrix(c(1,2),nrow=2))
})

test("Matrix transpose with [[1,2],[3,4]] works", c("r2.2"), {
  A<- matrix(c(1,2,3,4),nrow=2)
  expect_equal(transpose_matrix(A),matrix(c(1,3,2,4),nrow=2))
})
