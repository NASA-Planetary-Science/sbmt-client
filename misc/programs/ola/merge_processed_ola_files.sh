#!/bin/bash

#
# This script merges the "cube" files created by batch_process_all_ola.sh
# See the header of that script for details.
# 
#########################################################################
# This script has not yet been used operationally.
#########################################################################
#

# File extension
EXT=lidarcube

# Parent directory of folders containing the processed L2 file folders
DIR=/project/sbmtpipeline/processed/osirisrex/OLA/

OUT=$DIR/mergedCubes
rm -r $OUT
mkdir $OUT
sleep 2

# Get the max number of .lidarcube files by sorting all numerically in reverse and getting the basename of the first.
#FOUNDFILE=$(find /project/nearsdc/data/GASKELL/RQ36_V3/OLA/cubes/* -type f -name "*.$EXT" -printf '%f\n' | sort -n -r | head -n 1)
FOUNDFILE=$(find ${DIR} -type f -name "*.$EXT" -printf '%f\n' | sort -n -r | head -n 1)
MAXCUBES=$(basename "$FOUNDFILE" .$EXT)

echo "There are at most $MAXCUBES lidar cubes"

# Loop through each directory and merge all found .lidarcube files having the same base name

for FOLDER in $(find $DIR -type d -not -name "mergedCubes"); do
#for FOLDER in "$DIR"/*/ ; do
    for i in $(seq 0 $MAXCUBES) ; do
#        echo "Looking for lidar file $i.$EXT in $FOLDER"
        cat $FOLDER/$i.$EXT >> $OUT/$i.$EXT
    done
done
