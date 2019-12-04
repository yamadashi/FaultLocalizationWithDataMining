#include <stdio.h>
#include <stdlib.h>
#include "tritype.h"

//コマンドライン引数(出力ファイル, 入力1,2,3, 期待値)
int main(int argc, char *argv[])
{
    FILE *fp;
    int input[3];
    int expected;

    //出力ファイル
    if ((fp = fopen(argv[1], "a")) == NULL)
    {
        printf("can't open output file\n");
        exit(1);
    }

    //入力の数値変換
    for (int i = 0; i < 3; i++)
    {
        input[i] = atoi(argv[i + 2]);
    }
    expected = atoi(argv[5]);

    int result = tritype(input[0], input[1], input[2]);
    fprintf(fp, "%d,", (result == expected)); //成功なら1失敗なら0

    fclose(fp);

    return 0;
}