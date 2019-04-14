#include <stdio.h>
#include "hoge.h"

int main(int argc, char *argv[]) {
    
    for (int i = 0; i < 10; i++) {
        printf("%d\n",hoge(i));
    }

    return 0;
}