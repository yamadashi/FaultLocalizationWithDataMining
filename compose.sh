#!/bin/sh

outputfile="data.csv"
covfile="tritype.c.gcov"
testfile="test"

function func() {
    cd `dirname $0`

    cd target
    gcc -coverage -o main main.c tritype.c
    ./main.exe ../$outputfile $1 $2 $3 $4
    gcov tritype.c > /dev/null

    cd ../ProcTraceInfo
    java TraceInfoProcessor ../target/$covfile ../$outputfile

    cd ../target
    rm *.gcda *.gcno *.c.gcov
}

cd `dirname $0`
cat ./testdata/$testfile | while read line
do
    func $line
done