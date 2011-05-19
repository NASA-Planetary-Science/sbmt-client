#!/bin/bash

cd `dirname $0`
. setup.sh


AMICA_DIR=/project/nearsdc/data/ITOKAWA/AMICA/images

# Create a list of all fit files
find -L $AMICA_DIR -name "*.fit" -type f | sort > /project/nearsdc/data/internal/allAmicaFiles.txt


$JAVA_COMMAND edu.jhuapl.near.dbgen.AmicaBackplanesGenerator /project/nearsdc/data/internal/allAmicaFiles.txt
