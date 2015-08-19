#include <check.h>
#include "tmc-check.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <time.h>
#include <math.h>
#include "../src/source.h"


START_TEST(test_three_lines)
{
    freopen("mockoutput", "w", stdout);
    three_lines();
    fclose(stdout);
    FILE *fp = fopen("mockoutput", "r");
    char str [100] = { 0 };
    fread(str, 99, 1, fp);
    char infostr[100] = "";
    char *rightstr = "June\nJuly\nAugust\n";
    int ret = mycompare(str, rightstr, infostr);
    fail_unless(!ret, "[Task 1.1] Your output:\n%s\nReference output:\n%s\nReason: %s\n",
            str, rightstr, infostr);
    fclose(fp);
}
END_TEST

int main(int argc, const char *argv[])
{
    srand((unsigned)time(NULL));
	Suite *s = suite_create("Test-1.1-4");

	tmc_register_test(s, test_three_lines, "1.1");

	return tmc_run_tests(argc, argv, s);
}
