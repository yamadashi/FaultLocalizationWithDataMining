#!/bin/bash
stdpath=$PWD/`dirname $0`
inputfile="data.txt"
outputfile="adjust.txt"

cd $stdpath
javac -encoding UTF8 AdjustRatio.java

ls -1 $stdpath/../targets | while read target
do
    datadir=$stdpath/../targets/$target/data
    java AdjustRatio $datadir/$inputfile $datadir/$outputfile
done