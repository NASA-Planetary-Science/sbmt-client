#!/bin/bash

#
# This script runs MSIBackplanesGenerator.java.
#

if [ -z "$SBMTROOT" ]; then
    echo "ERROR: SBMTROOT is undefined!"
    exit 1
fi

echo -e "MSIBackplanesGenerator, SBMTROOT is set to ${SBMTROOT}"

export DYLD_LIBRARY_PATH="$SBMTROOT/lib:$DYLD_LIBRARY_PATH"
HEADLESS="-Djava.awt.headless=true"

if [ "$(uname)" == "Darwin" ]; then
    MEMSIZE=`sysctl hw.memsize | awk '{print int($2/1024)}'`
    export LD_LIBRARY_PATH="$SBMTROOT/lib:$SBMTROOT/lib/mac64:$LD_LIBRARY_PATH"
elif [ "$(uname)" == "Linux" ]; then
#    MEMSIZE=`grep MemTotal /proc/meminfo | awk '{print $2}'`
    MEMSIZE="$(/bin/grep MemTotal /proc/meminfo | awk '{print $2}')"
    export LD_LIBRARY_PATH="$SBMTROOT/lib:$SBMTROOT/lib/linux64:$LD_LIBRARY_PATH"
else 
    echo "Unsupported operating system, exiting script MSIBackplanesGenerator"
    exit 1
fi

echo "MSIBackplanesGenerator dir is $DIR"
echo "MSIBackplanesGenerator ldlibrarypath is $LD_LIBRARY_PATH"
echo "MSIBackplanesGenerator path is $PATH"
echo "MSIBackplanesGenerator sbmtroot is $SBMTROOT"
echo "MSIBackplanesGenerator memsize is $MEMSIZE"

#/project/nearsdc/software/java/x86_64/latest/bin/java -Xmx${MEMSIZE}K $HEADLESS "-Djava.library.path=$DIR/../lib" -cp "$SBMTROOT/lib/near.jar:$SBMTROOT/lib/*.jar" edu.jhuapl.sbmt.tools.MSIBackplanesGenerator $@
/project/nearsdc/software/java/x86_64/latest/bin/java -Xmx${MEMSIZE}K $HEADLESS "-Djava.library.path=$SBMTROOT/lib/linux64" -cp "$SBMTROOT/lib/near.jar:$SBMTROOT/lib/*.jar:$SBMTROOT/src" edu.jhuapl.sbmt.tools.MSIBackplanesGenerator $@

#Move the output backplanes file to a different folder here.