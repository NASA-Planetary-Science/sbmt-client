#!/bin/bash

# This script copies OLA level 2 data from the source directory to the pipeline directory,
# creates a text file containing a list of these files, and processes the data for SBMT.
# This script was modified from generate_nlr_cubes.sh. 
#chmod u=rwx,g=rx,o=r /project/sbmtpipeline/processed/osirisrex/OLA/generate_ola_cubes.sh

SBMT_SRC_DIR=/homes/nguyel1/sbmt_workspace/sbmt
SOURCE_INPUT_DIR=/project/osiris/altwg/outputdata/bennu_simulated_l2/
SBMT_DATA_DIR_PREFIX=/project/nearsdc/data
SBMT_DATA_DIR_SUFFIX=/GASKELL/RQ36_V3/OLA
SBMT_DATA_DIR=$SBMT_DATA_DIR_PREFIX$SBMT_DATA_DIR_SUFFIX
INPUT_DIR=/project/sbmtpipeline/rawdata/osirisrex/OLA
OUTPUT_DIR=/project/sbmtpipeline/processed/osirisrex/OLA

# Copy source data to pipeline directory
#cp $SOURCE_INPUT_DIR/*.l2 $INPUT_DIR

# Compress level 2 files (keep original files) and move zipped to model directory.
#cd $SOURCE_INPUT_DIR
#for file in *.l2 ; do
#    gzip < "$file" > $INPUT_DIR/"$file".gz
#done

# Create a list of all level 2 files for processing
find -L $INPUT_DIR -maxdepth 1 -name "*.l2" -type f | sort > $OUTPUT_DIR/allOlaFiles.txt

# This script must be run from the SBMT workspace top-level directory
cd $SBMT_SRC_DIR

VTK_DIR=$SBMT_SRC_DIR/build/sbmt-extras
VTK_PATH=/project/nearsdc/software/vtk_all_platforms/linux64
JAVA=/disks/d0364/software/jdk1.8.0_51/jre/bin/java 
export LD_LIBRARY_PATH=$JAVA:JAVA/xawt:$VTK_PATH:$VTK_PATH/bin:$LD_LIBRARY_PATH

JAVA_LIB=/disks/d0364/software/jdk1.8.0_51/jre/lib/amd64

COMMAND="$JAVA -Djava.awt.headless=true -Djava.library.path=$VTK_PATH:$JAVA_LIB -Dfile.encoding=UTF-8 -classpath $SBMT_SRC_DIR/bin:$SBMT_SRC_DIR/lib/vtksb.jar:$SBMT_SRC_DIR/lib/spice-N0065.jar:$SBMT_SRC_DIR/lib/commons-io-2.0.jar:$SBMT_SRC_DIR/build/sbmt-extras/lib/vtk-6.2.jar:$SBMT_SRC_DIR/lib/jide-oss-2.7.3.jar:$SBMT_SRC_DIR/lib/joda-time-1.6.jar:$SBMT_SRC_DIR/lib/mysql-connector-java-5.1.10-bin.jar edu.jhuapl.near.tools.OlaCubesGeneratorSeries"

echo -e "$COMMAND \n"

cd $OUTPUT_DIR

# Execute the lidar cube generator
$COMMAND

chmod u=rwx,g=rx,o=r $OUTPUT_DIR/*
chmod u=rwx,g=rx,o=r $OUTPUT_DIR/cubes/*

# Set the path prefix appropriately for the list of all OLA files that will be in the live directory
cd $OUTPUT_DIR
touch allOlaFiles2.txt
awk -v old=${INPUT_DIR} -v new=${SBMT_DATA_DIR_SUFFIX} '{gsub(old,new);print>"allOlaFiles2.txt"}' allOlaFiles.txt
mv $OUTPUT_DIR/allOlaFiles2.txt $OUTPUT_DIR/allOlaFiles_moveToLiveDir.txt

# Final move of processed data. Do this by hand.
#cp $INPUT_DIR/*.l2 $SBMT_DATA_DIR
#cp $OUTPUT_DIR/cubes/* $SBMT_DATA_DIR/cubes
#cp $OUTPUT_DIR/allOlaFiles_moveToLiveDir.txt $SBMT_DATA_DIR/allOlaFiles.txt

