#!/bin/bash

# Script for running the create_info_files program
cd `dirname $0`

# Create a list of all label files
HRSC_DIR=/project/nearsdc/data/PHOBOS/IMAGING/hrsc
HRSC_LABEL_FILES=/project/nearsdc/data/test/allHrscFiles.txt
find -L $HRSC_DIR -name "*label" -type f | sort > $HRSC_LABEL_FILES

INFO_DIR=/project/nearsdc/data/PHOBOS/IMAGING/infofiles
rm -rf $INFO_DIR
mkdir -p $INFO_DIR
echo ./create_info_files PHOBOS kernels.txt $HRSC_LABEL_FILES $INFO_DIR
./create_info_files PHOBOS kernels.txt $HRSC_LABEL_FILES $INFO_DIR
