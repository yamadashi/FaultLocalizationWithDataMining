#!/bin/bash
covfile="tritype.c.gcov"
testfile="testdata"
stdpath=$PWD/`dirname $0`
outputfile="data.txt"


function exec() {
    cd $targetdir
    ./main.exe $outputdir/$outputfile $1 $2 $3 $4
    gcov tritype.c > /dev/null

    cd $stdpath/ProcTraceInfo
    java TraceInfoProcessor $targetdir/$covfile $outputdir/$outputfile

    cd $targetdir
    rm *.gcda *.c.gcov
}

function func() {
    #ビルド
    cd $targetdir
    gcc -coverage -o main.exe main.c tritype.c
    cd $stdpath/ProcTraceInfo
    javac -encoding UTF8 TraceInfoProcessor.java

    mkdir $outputdir
    rm $outputdir/$outputfile
    touch $outputdir/$outputfile
    
    #各テスト入力について実行
    cat $stdpath/testdata/$testfile | while read line
    do
        exec $line
    done
}

ls -1 $stdpath/targets | while read target
do
    targetdir=$stdpath/targets/$target
    outputdir=$targetdir/data
    func
done