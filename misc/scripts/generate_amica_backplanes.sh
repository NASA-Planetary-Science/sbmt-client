#!/bin/bash

cd `dirname $0`
. setup.sh


INERTIAL_FILE=/project/nearsdc/data/internal/INERTIAL.TXT
AMICA_DIR=/project/nearsdc/data/ITOKAWA/AMICA/images
AMICA_FIT_FILES=/project/nearsdc/data/internal/allAmicaFiles.txt

# Create a list of all fit files
find -L $AMICA_DIR -name "*.fit" -type f | sort > $AMICA_FIT_FILES

./split.pl $AMICA_FIT_FILES 5


COMMAND_FILE=amicarun.mk
rm -f $COMMAND_FILE

echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.00 $INERTIAL_FILE 1 > amicalog1.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.00 $INERTIAL_FILE 2 > amicalog2.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.01 $INERTIAL_FILE 1 >> amicalog1.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.01 $INERTIAL_FILE 2 >> amicalog2.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.02 $INERTIAL_FILE 1 >> amicalog1.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.02 $INERTIAL_FILE 2 >> amicalog2.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.03 $INERTIAL_FILE 1 >> amicalog1.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.03 $INERTIAL_FILE 2 >> amicalog2.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.04 $INERTIAL_FILE 1 >> amicalog1.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.04 $INERTIAL_FILE 2 >> amicalog2.txt 2>&1" >> $COMMAND_FILE

./run_jobs_in_parallel.py $COMMAND_FILE 2
