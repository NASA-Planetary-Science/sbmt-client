#!/bin/bash

# Setup paths to java
export JAVA_HOME=/project/nearsdc/software/java/x86_64/jdk1.6.0_25
export PATH=$JAVA_HOME/bin:$PATH
export LD_LIBRARY_PATH=$JAVA_HOME/jre/lib/amd64:$JAVA_HOME/jre/lib/amd64/xawt

# Setup virtual display since java has problems in headless mode                                                                              
killall Xvfb
Xvfb :20 &
export DISPLAY=":20"


# This script must be run from this directory
CUR_DIR=/project/nearsdc/src/near_vis/trunk
cd $CUR_DIR


AMICA_DIR=/project/nearsdc/data/ITOKAWA/AMICA/images

# Create a list of all fit files
find -L $AMICA_DIR -name "*.fit" -type f | sort > /project/nearsdc/data/internal/allAmicaFiles.txt

COMMAND="java -Djava.library.path=/project/nearsdc/software/vtk_all_platforms/linux64:$JAVA_HOME/jre/lib/amd64/xawt -Dfile.encoding=UTF-8 -classpath $CUR_DIR/bin:$CUR_DIR/lib/vtksb.jar:$CUR_DIR/lib/vtk.jar:$CUR_DIR/lib/fits-1.04.0.jar:$CUR_DIR/lib/jide-oss-2.7.3.jar:$CUR_DIR/lib/joda-time-1.6.jar:$CUR_DIR/lib/mysql-connector-java-5.1.10-bin.jar edu.jhuapl.near.dbgen.AmicaDatabaseGeneratorSql /project/nearsdc/data/internal/allAmicaFiles.txt"

echo -e "$COMMAND \n"

$COMMAND 1 > sqlgeneration1.log 2>&1 &
$COMMAND 2 > sqlgeneration2.log 2>&1 &

wait
