#!/bin/bash

# Script for getting Ceres data from /project/dawn to the SBMT 
# data pipeline directory. Some filtering is probably needed,
# at least to match the .LBL files to the .FIT files.

DAWN_DATA_DIR=/project/dawn
CERES_DATA_DIR=$DAWN_DATA_DIR/data2
TMP_DIR=/project/sbmtpipeline/rawdata/dawn

find -L $CERES_DATA_DIR -name "*.FIT" -o -name "*.LBL" -not -name "*OPNAV*" -type f | xargs cp -u -t $TMP_DIR/ceres/fc
cd $TMP_DIR
ln -s $CERES_DATA_DIR/spice/ .
ln -s $DAWN_DATA_DIR/NAIF/ .