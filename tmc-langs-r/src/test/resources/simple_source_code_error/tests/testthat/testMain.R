library('testthat')

pointsForAllTests(c("r1"))

test("RetTrue works.", c("r1.1"), {
  expect_true(RetTrue())
})

test("RetOne works.", c("r1.2"), {
  expect_equal(RetOne(), 1)
})

test("Add works.", c("r1.3", "r1.4"), {
  expect_equal(Add(1, 1), 2)
  expect_equal(Add(0, 1), 1)
  expect_equal(Add(0, 0), 0)
  expect_equal(Add(5, 5), 10)
})
