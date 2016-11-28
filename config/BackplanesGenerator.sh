#!/bin/bash

#
# This script runs BackplanesGenerator.java.
#

if [ -z "$SBMTROOT" ]; then
    echo "ERROR: SBMTROOT is undefined!"
    exit 1
fi

echo -e "BackplanesGenerator, SBMTROOT is set to ${SBMTROOT}"

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
	echo "Unsupported operating system, exiting script BackplanesGenerator"
	exit 1
fi

echo "BackplanesGenerator dir is $DIR"
echo "BackplanesGenerator ldlibrarypath is $LD_LIBRARY_PATH"
echo "BackplanesGenerator path is $PATH"
echo "BackplanesGenerator sbmtroot is $SBMTROOT"
echo "BackplanesGenerator memsize is $MEMSIZE"

#/project/nearsdc/software/java/x86_64/latest/bin/java -Xmx${MEMSIZE}K $HEADLESS "-Djava.library.path=$DIR/../lib" -cp "$SBMTROOT/lib/near.jar:$SBMTROOT/lib/*.jar" edu.jhuapl.sbmt.tools.BackplanesGenerator $@
/project/nearsdc/software/java/x86_64/latest/bin/java -Xmx${MEMSIZE}K $HEADLESS "-Djava.library.path=$SBMTROOT/lib/linux64" -cp "$SBMTROOT/lib/near.jar:$SBMTROOT/lib/*.jar:$SBMTROOT/src" edu.jhuapl.sbmt.tools.BackplanesGenerator $@

#Move the output backplanes file to a different folder here.