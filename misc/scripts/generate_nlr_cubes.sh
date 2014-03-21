#!/bin/bash

# This script must be run from this directory
CUR_DIR=/disks/dg007/near/kahneg1/code/code/near_vis/trunk
cd $CUR_DIR


NLR_DIR=/disks/dg007/near/kahneg1/data/NLR

# Create a list of all fit files
#find -L $NLR_DIR -name "*.TAB.gz" -type f | sort > /disks/dg007/near/kahneg1/data/internal/allNlrFiles.txt

OUTPUT_DIR=/disks/dg007/near/kahneg1/data/NLR/per_cube

JAVA_PATH=/homes/kahneg1/programs/java/x86_64/jdk1.6.0_20/jre/bin/java

export LD_LIBRARY_PATH=/homes/kahneg1/programs/java/x86_64/jdk1.6.0_20/jre/lib/amd64:/homes/kahneg1/programs/java/x86_64/jdk1.6.0_20/jre/lib/amd64/xawt:$LD_LIBRARY_PATH


COMMAND="$JAVA_PATH -Djava.library.path=/disks/dg007/near/kahneg1/vtk_all_platforms/linux64:/homes/kahneg1/programs/java/x86_64/jdk1.6.0_20/jre/lib/amd64/xawt -Dfile.encoding=UTF-8 -classpath $CUR_DIR/bin:$CUR_DIR/lib/vtksb.jar:$CUR_DIR/lib/vtk.jar:$CUR_DIR/lib/jide-oss-2.7.3.jar:$CUR_DIR/lib/joda-time-1.6.jar:$CUR_DIR/lib/mysql-connector-java-5.1.10-bin.jar edu.jhuapl.near.tools.NLRCubesGenerator /disks/dg007/near/kahneg1/data/internal/allNlrFiles.txt $OUTPUT_DIR"

echo -e "$COMMAND \n"


$COMMAND
