#!/bin/bash
outputfile=$2
covfile="tritype.c.gcov"
testfile="testdata"
stdpath=$PWD/`dirname $0`
targetPG=$1
targetdir=$stdpath/targets/$targetPG


function func() {
    cd $targetdir
    ./main.exe $stdpath/$outputfile $1 $2 $3 $4
    gcov tritype.c > /dev/null

    cd $stdpath/ProcTraceInfo
    java TraceInfoProcessor $targetdir/$covfile $stdpath/$outputfile

    cd $targetdir
    rm *.gcda *.c.gcov
}

#ビルド
cd $targetdir
gcc -coverage -o main.exe main.c tritype.c
cd $stdpath/ProcTraceInfo
javac -encoding UTF8 TraceInfoProcessor.java

#各テスト入力について実行
cat $stdpath/testdata/$testfile | while read line
do
    func $line
done


# context:
# fail, pass, line1, line2, line3, ...