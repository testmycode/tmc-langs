#include <stdio.h>
#include "source.h"

int main()
{
    /* testing exercise 1.1 */
    printf("\n--- Exercise 1.1 ---\n");
    three_lines();
    
    /* testing exercise 1.2 */
    printf("\n--- Exercise 1.2 ---\n");
    printf("distOrigo(2.5, 5): %f\n", distOrigo(2.5, 5));  // should be 5.590170
    printf("distOrigo(0.1, 0.1): %f\n", distOrigo(0.1, 0.1));  // should be 0.141421
    
    /* testing exercise 1.3 */
    printf("\n--- Exercise 1.3 ---\n");
    simple_sum();

    /* testing exercise 1.4 */
    printf("\n--- Exercise 1.4 ---\n");
    printf("result: %f\n", simple_math());
    
    return 0;
}
