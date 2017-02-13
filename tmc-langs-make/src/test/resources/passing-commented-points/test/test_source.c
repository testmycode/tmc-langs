#include <check.h>
#include "tmc-check.h"
#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include "../src/source.h"

START_TEST(test_one)
{
    int res = one();
    fail_unless(res == 1, "[Task 1.1] one returned %d. Should have returned: %d",
            res, 1);
}
END_TEST

int main(int argc, const char *argv[])
{
    srand((unsigned)time(NULL));
	Suite *s = suite_create("Test-Passing");

	//tmc_register_test(s, test_one, "1");
	tmc_register_test(s, test_one, "2");
	/* tmc_register_test(s, test_one, "3");
	tmc_register_test(s, test_one, "4");
	tmc_register_test(s, test_one, "5");
	*/
	tmc_register_test(s, test_one, "//6");
	tmc_register_test(s, test_one, "/*7");
	tmc_register_test(s, test_one, "aaa\"");

	return tmc_run_tests(argc, argv, s);
}
