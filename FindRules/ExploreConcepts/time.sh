#!/bin/bash

STEP=50
SUM=0
TIMEFORMAT='%U'

for i in `seq $STEP`
do
    TMP=`(time make exec > /dev/null) 2>&1`
    SUM=`echo "scale=5; $SUM + $TMP" | bc`
done
echo "scale=5; $SUM/$STEP" | bc