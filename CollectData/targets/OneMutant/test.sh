#!/bin/bash

rm *.gcda *.gcov

gcc -coverage -o main.exe main.c tritype.c
./main.exe /dev/null 1 1 1 1
gcov tritype.c > /dev/null