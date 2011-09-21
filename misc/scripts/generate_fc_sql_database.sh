#!/bin/bash

cd `dirname $0`
. setup.sh


FC_DIR=/project/nearsdc/data-apl/VESTA/FC/images
FC_FIT_FILES=/project/nearsdc/data/internal/allFcFiles.txt

# Create a list of all fit files
find -L $FC_DIR -name "*.FIT" -type f | sort > $FC_FIT_FILES


$JAVA_COMMAND edu.jhuapl.near.server.VestaDatabaseGeneratorSql $FC_FIT_FILES 1 > sqlgeneration-vesta1.log 2>&1 &
$JAVA_COMMAND edu.jhuapl.near.server.VestaDatabaseGeneratorSql $FC_FIT_FILES 2 > sqlgeneration-vesta2.log 2>&1 &
$JAVA_COMMAND edu.jhuapl.near.server.VestaDatabaseGeneratorSql $FC_FIT_FILES 3 > sqlgeneration-vesta3.log 2>&1 &
$JAVA_COMMAND edu.jhuapl.near.server.VestaDatabaseGeneratorSql $FC_FIT_FILES 4 > sqlgeneration-vesta4.log 2>&1 &

wait
