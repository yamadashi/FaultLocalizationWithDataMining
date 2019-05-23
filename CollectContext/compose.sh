#!/bin/sh
outputfile="data.csv"
covfile="tritype.c.gcov"
testfile="testdata"
#stdpath="${PWD}/`dirname $0`"
stdpath=$PWD/`dirname $0`

function func() {
    cd $stdpath/target
    ./main.exe ../$outputfile $1 $2 $3 $4
    gcov tritype.c > /dev/null

    cd $stdpath/ProcTraceInfo
    java TraceInfoProcessor ../target/$covfile ../$outputfile

    cd $stdpath/target
    rm *.gcda *.c.gcov
}

#ビルド
cd $stdpath/target
gcc -coverage -o main.exe main.c tritype.c
cd $stdpath/ProcTraceInfo
javac -encoding UTF8 TraceInfoProcessor.java

#各テスト入力について実行
cat $stdpath/testdata/$testfile | while read line
do
    func $line
done