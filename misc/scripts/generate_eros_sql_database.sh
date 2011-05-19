#!/bin/bash

# This script must be run from this directory
CUR_DIR=/disks/dg007/near/kahneg1/code/code/near_vis/trunk
cd $CUR_DIR


FITS_DIR=/disks/dg007/near/kahneg1/data/MSI

# Create a list of all fit files
#find -L $FITS_DIR -name "*.FIT" -type f | sort > /disks/dg007/near/kahneg1/data/internal/allMsiFiles.txt

NIS_DIR=/disks/dg007/near/kahneg1/data/NIS

# Create a list of all nis files
#find -L $NIS_DIR -name "*.NIS" -type f | sort > /disks/dg007/near/kahneg1/data/internal/allNisFiles.txt


JAVA_PATH=/homes/kahneg1/programs/java/x86_64/jdk1.6.0_20/jre/bin/java

export LD_LIBRARY_PATH=/homes/kahneg1/programs/java/x86_64/jdk1.6.0_20/jre/lib/amd64:/homes/kahneg1/programs/java/x86_64/jdk1.6.0_20/jre/lib/amd64/xawt:$LD_LIBRARY_PATH


COMMAND="$JAVA_PATH -Djava.library.path=/disks/dg007/near/kahneg1/vtk_all_platforms/linux64 -Dfile.encoding=UTF-8 -classpath $CUR_DIR/bin:$CUR_DIR/lib/vtksb.jar:$CUR_DIR/lib/vtk.jar:$CUR_DIR/lib/jide-oss-2.7.3.jar:$CUR_DIR/lib/joda-time-1.6.jar:$CUR_DIR/lib/mysql-connector-java-5.1.10-bin.jar edu.jhuapl.near.server.ErosDatabaseGeneratorSql /disks/dg007/near/kahneg1/data/internal/allMsiFiles.txt /disks/dg007/near/kahneg1/data/internal/allNisFiles.txt"

echo -e "$COMMAND \n"


#$COMMAND $1

$COMMAND 1 > sqlgeneration1.log 2>&1 &
$COMMAND 2 > sqlgeneration2.log 2>&1 &
$COMMAND 3 > sqlgeneration3.log 2>&1 &
$COMMAND 4 > sqlgeneration4.log 2>&1 &

wait
