#!/bin/bash

# Script for running the create_info_files program
cd `dirname $0`

# Create a list of all label files
VIKING_DIR=/project/nearsdc/data/DEIMOS/IMAGING/viking
VIKING_LABEL_FILES=/project/nearsdc/data/test/allVikingFiles.txt
find -L $VIKING_DIR -name "*label" -type f | sort > $VIKING_LABEL_FILES

INFO_DIR=/project/nearsdc/data/DEIMOS/IMAGING/infofiles
rm -rf $INFO_DIR
mkdir -p $INFO_DIR
echo ./create_info_files DEIMOS kernels.txt $VIKING_LABEL_FILES $INFO_DIR
./create_info_files DEIMOS kernels.txt $VIKING_LABEL_FILES $INFO_DIR
