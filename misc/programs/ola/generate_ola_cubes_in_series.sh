#!/bin/bash

#
# This script copies OLA level 2 data from the source directory to the pipeline directory,
# creates a text file containing a list of these files, and processes the data for SBMT.
# This script was modified from generate_nlr_cubes.sh. It processes the OLA data on a 
# single machine in series. Script batch_process_all_ola_files.sh is intended to replace
# this script to process large numbers of OLA level 2 files.
#
# Pass the path to the SBMT source code on the command line.
#


# Path to SBMT workspace
SBMT_SRC_DIR=$1
#SBMT_SRC_DIR=/homes/nguyel1/sbmt_workspace/sbmt

SBMT_DATA_DIR_SUFFIX=/GASKELL/RQ36_V3/OLA
INPUT_DIR=/project/sbmtpipeline/rawdata/osirisrex/OLA
OUTPUT_DIR=/project/sbmtpipeline/processed/osirisrex/OLA

mkdir -p $OUTPUT_DIR

# Copy source data to pipeline directory (consider replacing "cp" with "rsync")
#cp -r /project/osiris/altwg/outputdata/bennu_simulated_l2/Phase06_DS $INPUT_DIR
#cp -r /project/osiris/altwg/outputdata/bennu_simulated_l2/Phase07_PS $INPUT_DIR

# Create a list of all level 2 files for processing
find -L $INPUT_DIR -maxdepth 1 -name "*.l2" -type f | sort > $OUTPUT_DIR/allOlaFiles.txt

# This script must be run from the SBMT workspace top-level directory
cd $SBMT_SRC_DIR

VTK_DIR=$SBMT_SRC_DIR/build/sbmt-extras
VTK_PATH=/project/nearsdc/software/vtk_all_platforms/linux64
JAVA=/disks/d0364/software/jdk1.8.0_51/jre/bin/java 
export LD_LIBRARY_PATH=$JAVA:JAVA/xawt:$VTK_PATH:$VTK_PATH/bin:$LD_LIBRARY_PATH

JAVA_LIB=/disks/d0364/software/jdk1.8.0_51/jre/lib/amd64

COMMAND="$JAVA -Djava.awt.headless=true -Djava.library.path=$VTK_PATH:$JAVA_LIB -Dfile.encoding=UTF-8 -classpath $SBMT_SRC_DIR/build/classes:$SBMT_SRC_DIR/lib/vtksb.jar:$SBMT_SRC_DIR/lib/spice-N0065.jar:$SBMT_SRC_DIR/lib/commons-io-2.0.jar:$SBMT_SRC_DIR/build/sbmt-extras/lib/vtk-6.2.jar:$SBMT_SRC_DIR/lib/jide-oss-2.7.3.jar:$SBMT_SRC_DIR/lib/joda-time-1.6.jar:$SBMT_SRC_DIR/lib/mysql-connector-java-5.1.10-bin.jar edu.jhuapl.near.tools.OlaCubesGeneratorSeries"

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

#
# Final move of processed data to live directory:
#
# For new *.l2 files (and to keep existing files):
#   1)  Rsync the *.l2 files to /project/nearsdc/data/GASKELL/RQ36_V3/OLA
#   2)  *Merge* the new lidar cube files together with those in /project/nearsdc/data/GASKELL/RQ36_V3/OLA/cubes/ 
#       directory (do not rsync if adding more data and keeping existing)
#   3)  *Append* allOlaFiles_moveToLiveDir.txt content to the end of /project/nearsdc/data/GASKELL/RQ36_V3/OLA/allOlaFiles.txt
#   4)  chmod 775 on all files
#
# For newer versions of existing *.l2 files: 
# Will need to reprocess the entire dataset, then do the above steps except overwrite instead of append/merge.
#

