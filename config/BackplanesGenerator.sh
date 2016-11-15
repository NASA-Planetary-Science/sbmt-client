#!/bin/bash
#export SBMTROOT=/project/sbmtpipeline/sbmt/
if [ -z "$SBMTROOT" ]; then
    echo "ERROR: SBMTROOT is undefined!"
    exit 1
fi
#DIR=`dirname "$0"`
#echo "DIR is $DIR"

#export PATH="$DIR:$PATH"
export DYLD_LIBRARY_PATH="$SBMTROOT/lib:$DYLD_LIBRARY_PATH"
export LD_LIBRARY_PATH="$SBMTROOT/lib:$SBMTROOT/lib/linux64:$LD_LIBRARY_PATH"
export HEADLESS=""
export MEMSIZE=""
if [ "$(uname)" == "Darwin" ]; then
    export HEADLESS="-Djava.awt.headless=true"
    export MEMSIZE=`sysctl hw.memsize | awk '{print int($2/1024)}'`
elif [ "$(uname)" == "Linux" ]; then
    export MEMSIZE=`grep MemTotal /proc/meminfo | awk '{print $2}'`
fi

echo "BackplanesGenerator dir is $DIR"
echo "BackplanesGenerator ldlibrarypath is $LD_LIBRARY_PATH"
echo "BackplanesGenerator path is $PATH"
echo "BackplanesGenerator sbmtroot is $SBMTROOT"
echo "BackplanesGenerator memsize is $MEMSIZE"

#/project/nearsdc/software/java/x86_64/latest/bin/java -Xmx${MEMSIZE}K $HEADLESS "-Djava.library.path=$DIR/../lib" -cp "$SBMTROOT/lib/near.jar:$SBMTROOT/lib/*.jar" edu.jhuapl.sbmt.tools.BackplanesGenerator $@
/project/nearsdc/software/java/x86_64/latest/bin/java -Xmx${MEMSIZE}K $HEADLESS "-Djava.library.path=$SBMTROOT/lib/linux64" -cp "$SBMTROOT/lib/near.jar:$SBMTROOT/lib/*.jar:$SBMTROOT/src" edu.jhuapl.sbmt.tools.BackplanesGenerator $@
