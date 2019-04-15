#include <stdio.h>
#include <stdlib.h>
#include "tritype.h"

//コマンドライン引数(出力ファイル, 入力1,2,3, 期待値) 
int main(int argc, char *argv[]) {
    
    FILE* fp;
    int input[3];
    int expected;

    //出力ファイル名
    if ((fp = fopen(argv[0], "a") == NULL) {
        printf("出力ファイルがオープンできませんでした\n");
        exit(1);
    }

    //入力の数値変換
    for (int i = 0; i < 3; i++) {
        input[i] = atoi(argv[i+1]);
    }
    expected = atoi(argv[4]);
    
    int result = tritype(input[1], input[2], input[3]);
    fprintf(fp, "%d,%d,", (result == expected), !(result==expected)); //成功,失敗,

    fclose(fp);

    return 0;
}