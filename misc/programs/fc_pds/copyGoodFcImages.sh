#!/bin/bash

# Copy the good FC images to the live directory in /project/nearsdc
# Execute this after a successful run of run_ceres.sh, which 
# generates the list of good FC image, uniqFcFilesFullPath.txt.
#

cd `dirname $0`

DATA_DIR=/project/sbmtpipeline/rawdata/dawn/ceres
OUTPUT_DIR=/project/sbmtpipeline/processed/dawn/ceres
FC_DIR=/project/nearsdc/data/GASKELL/CERES/FC/
LIVE_DIR=$FC_DIR/images

# Copy the good images to the live directory 
for fit_file in $(cat $OUTPUT_DIR/uniqFcFilesFullPath.txt) ; do
  cp $fit_file $LIVE_DIR
done;

cp $OUTPUT_DIR/uniqFcFiles.txt $FC_DIR