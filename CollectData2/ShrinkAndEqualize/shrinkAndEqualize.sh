#!/bin/bash
stdpath=$PWD/`dirname $0`
inputfile="data.txt"
outputfile="shrink.txt"

cd $stdpath
javac -encoding UTF8 ShrinkAndEqualize.java

ls -1 $stdpath/../targets | while read target
do
    datadir=$stdpath/../targets/$target/data
    java ShrinkAndEqualize $datadir/$inputfile $datadir/$outputfile
done