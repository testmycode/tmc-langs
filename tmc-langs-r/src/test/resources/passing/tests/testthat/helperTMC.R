
#Sets the points for all tests to global environment, wherefrom they can
#be retrieved.
pointsForAllTests <- function(points) {
  .GlobalEnv$points_for_all_tests <- points
}

#The test that wraps around test_that()-method and stores the points
#to global environment.
test <- function(desc, points, code) {
  .GlobalEnv$points[[desc]] <- points
  test_that(desc, code)
}
