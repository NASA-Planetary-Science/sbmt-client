#!/bin/bash

# This script was modified from generate_nlr_cubes.sh

DATA_DIR=/project/sbmtpipeline/rawdata/osirisrex/OLA
OUTPUT_DIR=/project/sbmtpipeline/processed/osirisrex/OLA

# Compress level 2 files
#cd $DATA_DIR
#for file in *.l2 ; do
#    gzip < "$file" > $OUTPUT_DIR/"$file".gz
#done

# This script must be run from the SBMT workspace top-level directory
CUR_DIR=/homes/nguyel1/sbmt_workspace/sbmt
VTK_DIR=$CUR_DIR/build/sbmt-extras
cd $CUR_DIR

# Create a list of all level 2 files
find -L $OUTPUT_DIR -name "*.l2.gz" -type f | sort > $OUTPUT_DIR/allOlaFiles.txt

VTK_PATH=/project/nearsdc/software/vtk_all_platforms/linux64
JAVA=/disks/d0364/software/jdk1.8.0_51/jre/bin/java 
export LD_LIBRARY_PATH=$JAVA:JAVA/xawt:$VTK_PATH:$VTK_PATH/bin:$LD_LIBRARY_PATH

JAVA_LIB=/disks/d0364/software/jdk1.8.0_51/jre/lib/amd64

COMMAND="$JAVA -Djava.awt.headless=true -Djava.library.path=$VTK_PATH:$JAVA_LIB -Dfile.encoding=UTF-8 -classpath $CUR_DIR/bin:$CUR_DIR/lib/vtksb.jar:$CUR_DIR/build/sbmt-extras/lib/vtk-6.2.jar:$CUR_DIR/lib/jide-oss-2.7.3.jar:$CUR_DIR/lib/joda-time-1.6.jar:$CUR_DIR/lib/mysql-connector-java-5.1.10-bin.jar edu.jhuapl.near.tools.OlaCubesGenerator $OUTPUT_DIR/allOlaFiles.txt $OUTPUT_DIR/cubes"

echo -e "$COMMAND \n"
chmod u=rwx,g=rx,o=r /project/sbmtpipeline/processed/osirisrex/OLA/generate_ola_cubes.sh

$COMMAND
