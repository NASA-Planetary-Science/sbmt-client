#!/bin/bash

#
# This script generates "cube" files for SBMT for a single OLA level 2 file.
# See the header of batch_process_all_ola_files.sh for details.
#
#########################################################################
# This script has not yet been used operationally.
#########################################################################
#

# The L2 file basename is passed to this script as a parameter
L2FILE=$1

SBMT_SRC_DIR=/homes/nguyel1/sbmt_workspace/sbmt
INPUT_DIR=/project/sbmtpipeline/rawdata/osirisrex/OLA
OUTPUT_DIR=/project/sbmtpipeline/processed/osirisrex/OLA

# This script must be run from the SBMT workspace top-level directory
cd $SBMT_SRC_DIR

VTK_DIR=$SBMT_SRC_DIR/build/sbmt-extras
VTK_PATH=/project/nearsdc/software/vtk_all_platforms/linux64
JAVA=/disks/d0364/software/jdk1.8.0_51/jre/bin/java 
export LD_LIBRARY_PATH=$JAVA:JAVA/xawt:$VTK_PATH:$VTK_PATH/bin:$LD_LIBRARY_PATH

JAVA_LIB=/disks/d0364/software/jdk1.8.0_51/jre/lib/amd64

COMMAND="$JAVA -Djava.awt.headless=true -Djava.library.path=$VTK_PATH:$JAVA_LIB -Dfile.encoding=UTF-8 -classpath $SBMT_SRC_DIR/bin:$SBMT_SRC_DIR/lib/vtksb.jar:$SBMT_SRC_DIR/lib/spice-N0065.jar:$SBMT_SRC_DIR/lib/commons-io-2.0.jar:$SBMT_SRC_DIR/build/sbmt-extras/lib/vtk-6.2.jar:$SBMT_SRC_DIR/lib/jide-oss-2.7.3.jar:$SBMT_SRC_DIR/lib/joda-time-1.6.jar:$SBMT_SRC_DIR/lib/mysql-connector-java-5.1.10-bin.jar edu.jhuapl.near.tools.OlaCubesGenerator $L2FILE"

echo -e "$COMMAND \n"

#Not sure if I want this, OUTPUT_DIR should be the folder whose name is the lidar file basename. OlaCubesGenerator will create the folder.
cd $OUTPUT_DIR

# Execute the lidar cube generator
$COMMAND


