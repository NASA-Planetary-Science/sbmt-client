#!/bin/bash

# This script generates lidar data cubes for SBMT for a single OLA level 2 file.
# This script is called from run_generate_ola_cubes.sh to process all of the L2
# files using the cluster. 
#chmod u=rwx,g=rx,o=r /project/sbmtpipeline/processed/osirisrex/OLA/generate_ola_cubes.sh


L2FILE=$1

SBMT_SRC_DIR=/homes/nguyel1/sbmt_workspace/sbmt
SOURCE_INPUT_DIR=/project/osiris/altwg/outputdata/bennu_simulated_l2/
SBMT_DATA_DIR_PREFIX=/project/nearsdc/data
SBMT_DATA_DIR_SUFFIX=/GASKELL/RQ36_V3/OLA
SBMT_DATA_DIR=$SBMT_DATA_DIR_PREFIX$SBMT_DATA_DIR_SUFFIX
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

cd $OUTPUT_DIR

# Execute the lidar cube generator
$COMMAND


