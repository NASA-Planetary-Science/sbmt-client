#!/bin/bash

# This script must be run from this directory
CUR_DIR=/disks/dg007/near/kahneg1/code/code/near_vis/trunk
cd $CUR_DIR


MSI_DIR=/disks/dg007/near/kahneg1/data/MSI

# Create a list of all msi files
#find -L $MSI_DIR -name "*.FIT" -type f | sort > /disks/dg007/near/kahneg1/data/internal/allMsiFiles.txt

JAVA_PATH=/homes/kahneg1/programs/java/x86_64/jdk1.6.0_20/jre/bin/java
export LD_LIBRARY_PATH=/homes/kahneg1/programs/java/x86_64/jdk1.6.0_20/jre/lib/amd64:/homes/kahneg1/programs/java/x86_64/jdk1.6.0_20/jre/lib/amd64/xawt:$LD_LIBRARY_PATH

# -agentlib:hprof=file=snapshot.hprof,format=a,depth=10
COMMAND="$JAVA_PATH -Djava.library.path=/disks/dg007/near/kahneg1/vtk_all_platforms/linux64 -Dfile.encoding=UTF-8 -classpath $CUR_DIR/bin:$CUR_DIR/lib/vtksb.jar:$CUR_DIR/lib/vtk.jar:$CUR_DIR/lib/jide-oss-2.7.3.jar:$CUR_DIR/lib/joda-time-1.6.jar:$CUR_DIR/lib/mysql-connector-java-5.1.10-bin.jar edu.jhuapl.near.dbgen.MSIFootprintGenerator"

echo -e "$COMMAND \n"

#$COMMAND 0

for i in $@
do
    $COMMAND /disks/dg007/near/kahneg1/data/internal/allMsiFiles.txt.$i 3 >> msifootprintgen_res3.log.$i 2>&1 &
done
wait

for i in $@
do
    $COMMAND /disks/dg007/near/kahneg1/data/internal/allMsiFiles.txt.$i 2 >> msifootprintgen_res2.log.$i 2>&1 &
done
wait

for i in $@
do
    $COMMAND /disks/dg007/near/kahneg1/data/internal/allMsiFiles.txt.$i 1 >> msifootprintgen_res1.log.$i 2>&1 &
done
wait

for i in $@
do
    $COMMAND /disks/dg007/near/kahneg1/data/internal/allMsiFiles.txt.$i 0 >> msifootprintgen_res0.log.$i 2>&1 &
done
wait
