#!/bin/bash

make build

ls data/tritype/ -1 | while read each_case; do
    outputdir=output/tritype/${each_case}
    outputfile=${outputdir}/output.txt
    # make directory
    mkdir -p ${outputdir} 2> /dev/null
    # reset output file
    rm ${outputfile} 2> /dev/null
    touch ${outputfile}
    chmod 777 ${outputfile}

    make exec \
    INPUT=data/tritype/${each_case}/data/data.txt \
    MINCOOCC=90 \
    >> ${outputfile}
done
