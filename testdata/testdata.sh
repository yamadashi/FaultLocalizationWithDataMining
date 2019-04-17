#!/bin/sh

stdpath=$PWD/`dirname $0`
outputfile="testdata"

function random() {
    echo $(( $(od -vAn --width=4 -tu4 -N4 </dev/urandom) % 10 ))
}

function output() {
    local input1=`random`
    local input2=`random`
    local input3=`random`

    echo -n "$input1 $input2 $input3 " >> $stdpath/$outputfile

    #main.exeで書き込みしている
    $stdpath/src/main.exe $outputfile $input1 $input2 $input3
}

for i in `seq 400`
do
   output
done