#!/bin/bash

cd `dirname $0`
. setup.sh


FC_DIR=/project/nearsdc/data-apl/VESTA/FC/images
FC_FIT_FILES=/project/nearsdc/data/internal/allFcFiles.txt
SUMFILELIST=/project/nearsdc/data-apl/VESTA/FC/PICTLIST_HC1.TXT

# Create a list of all fit files
find -L $FC_DIR -name "*.FIT" -type f | sort > $FC_FIT_FILES


$JAVA_COMMAND edu.jhuapl.near.server.VestaDatabaseGeneratorSql $FC_FIT_FILES $SUMFILELIST 1 > sqlgeneration-vesta1.log 2>&1 &
$JAVA_COMMAND edu.jhuapl.near.server.VestaDatabaseGeneratorSql $FC_FIT_FILES $SUMFILELIST 2 > sqlgeneration-vesta2.log 2>&1 &
$JAVA_COMMAND edu.jhuapl.near.server.VestaDatabaseGeneratorSql $FC_FIT_FILES $SUMFILELIST 3 > sqlgeneration-vesta3.log 2>&1 &
$JAVA_COMMAND edu.jhuapl.near.server.VestaDatabaseGeneratorSql $FC_FIT_FILES $SUMFILELIST 4 > sqlgeneration-vesta4.log 2>&1 &

wait
