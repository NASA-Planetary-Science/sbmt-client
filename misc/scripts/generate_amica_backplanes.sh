#!/bin/bash

cd `dirname $0`
. setup.sh


AMICA_DIR=/project/nearsdc/data/ITOKAWA/AMICA/images
AMICA_FIT_FILES=/project/nearsdc/data/internal/allAmicaFiles.txt

# Create a list of all fit files
find -L $AMICA_DIR -name "*.fit" -type f | sort > $AMICA_FIT_FILES


$JAVA_COMMAND edu.jhuapl.near.dbgen.AmicaBackplanesGenerator $AMICA_FIT_FILES
