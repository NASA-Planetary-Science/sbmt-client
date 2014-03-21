#!/bin/bash

cd `dirname $0`
. setup.sh


INERTIAL_FILE=/project/nearsdc/data/internal/INERTIAL.TXT
AMICA_DIR=/project/nearsdc/data/ITOKAWA/AMICA/images
AMICA_FIT_FILES=/project/nearsdc/data/internal/allAmicaFiles.txt

# Create a list of all fit files
find -L $AMICA_DIR -name "*.fit" -type f | sort > $AMICA_FIT_FILES


$JAVA_COMMAND edu.jhuapl.near.tools.ItokawaDatabaseGeneratorSql $AMICA_FIT_FILES $INERTIAL_FILE 1 > sqlgeneration-itokawa1.log 2>&1 &
$JAVA_COMMAND edu.jhuapl.near.tools.ItokawaDatabaseGeneratorSql $AMICA_FIT_FILES $INERTIAL_FILE 2 > sqlgeneration-itokawa2.log 2>&1 &
$JAVA_COMMAND edu.jhuapl.near.tools.ItokawaDatabaseGeneratorSql $AMICA_FIT_FILES $INERTIAL_FILE 3 > sqlgeneration-itokawa3.log 2>&1 &
$JAVA_COMMAND edu.jhuapl.near.tools.ItokawaDatabaseGeneratorSql $AMICA_FIT_FILES $INERTIAL_FILE 4 > sqlgeneration-itokawa4.log 2>&1 &

wait
