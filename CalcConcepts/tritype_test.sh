#!/bin/bash

make build

filters=("none" "support" "lift" "all")
ls data/tritype/ -1 | while read each_case; do
    outputdir=output/tritype/${each_case}
    outputfile=${outputdir}/output.txt
    # make directory
    mkdir -p ${outputdir} 2> /dev/null
    # reset output file
    rm ${outputfile} 2> /dev/null
    touch ${outputfile}
    chmod 777 ${outputfile}

    for filter in ${filters[@]}; do
        echo -e "${filter}\n" >> ${outputfile}
        make exec \
        INPUT=data/tritype/${each_case}/data/data.csv \
        FILTER=${filter} \
        MINSUP=1 \
        >> ${outputfile}
        echo -e "\n\n" >> ${outputfile}
    done
done