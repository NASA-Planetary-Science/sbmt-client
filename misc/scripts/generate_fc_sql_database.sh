#!/bin/bash

cd `dirname $0`
. setup.sh


FC_DIR=/project/nearsdc/data-apl/VESTA/FC/images
FC_FIT_FILES=/project/nearsdc/data/internal/allFcFiles.txt

# Create a list of all fit files
find -L $FC_DIR -name "*.FIT" -type f | sort > $FC_FIT_FILES


$JAVA_COMMAND edu.jhuapl.near.server.VestaDatabaseGeneratorSql $FC_FIT_FILES 1 > sqlgeneration1.log 2>&1 &
$JAVA_COMMAND edu.jhuapl.near.server.VestaDatabaseGeneratorSql $FC_FIT_FILES 2 > sqlgeneration2.log 2>&1 &

wait
