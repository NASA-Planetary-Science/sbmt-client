#!/bin/bash

cd `dirname $0`
. setup.sh


INERTIAL_FILE=/project/nearsdc/data/ITOKAWA/AMICA/INERTIAL.TXT
AMICA_DIR=/project/nearsdc/data/ITOKAWA/AMICA/images
AMICA_FIT_FILES=/project/nearsdc/data/internal/allAmicaFiles.txt

# Create a list of all fit files
find -L $AMICA_DIR -name "*.fit" -type f | sort > $AMICA_FIT_FILES

./split.pl $AMICA_FIT_FILES 15


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
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.05 $INERTIAL_FILE 1 >> amicalog1.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.05 $INERTIAL_FILE 2 >> amicalog2.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.06 $INERTIAL_FILE 1 >> amicalog1.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.06 $INERTIAL_FILE 2 >> amicalog2.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.07 $INERTIAL_FILE 1 >> amicalog1.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.07 $INERTIAL_FILE 2 >> amicalog2.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.08 $INERTIAL_FILE 1 >> amicalog1.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.08 $INERTIAL_FILE 2 >> amicalog2.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.09 $INERTIAL_FILE 1 >> amicalog1.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.09 $INERTIAL_FILE 2 >> amicalog2.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.10 $INERTIAL_FILE 1 >> amicalog1.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.10 $INERTIAL_FILE 2 >> amicalog2.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.11 $INERTIAL_FILE 1 >> amicalog1.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.11 $INERTIAL_FILE 2 >> amicalog2.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.12 $INERTIAL_FILE 1 >> amicalog1.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.12 $INERTIAL_FILE 2 >> amicalog2.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.13 $INERTIAL_FILE 1 >> amicalog1.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.13 $INERTIAL_FILE 2 >> amicalog2.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.14 $INERTIAL_FILE 1 >> amicalog1.txt 2>&1" >> $COMMAND_FILE
echo "$JAVA_COMMAND edu.jhuapl.near.server.AmicaBackplanesGenerator $AMICA_FIT_FILES.14 $INERTIAL_FILE 2 >> amicalog2.txt 2>&1" >> $COMMAND_FILE

./run_jobs_in_parallel.py $COMMAND_FILE 2
