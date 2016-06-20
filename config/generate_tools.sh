#!/bin/bash

# This script generates individual scripts to run each of the tools.

if [ -z "$SBMTROOT" ]; then
    echo "ERROR: SBMTROOT is undefined!"
    exit 1
fi
 
set -e
set -o pipefail

INSTALL_DIR=$SBMTROOT
INSTALL_BIN_DIR=$INSTALL_DIR/bin
INSTALL_LIB_DIR=$INSTALL_DIR/lib

mkdir -p $INSTALL_DIR
mkdir -p $INSTALL_BIN_DIR
mkdir -p $INSTALL_LIB_DIR


# Generate Run Scripts
CLASSPATH='$SBMTROOT/lib/near.jar'
for f in lib/*.jar ; do
    f=`basename $f`
    CLASSPATH="$CLASSPATH:\$SBMTROOT/lib/$f"
done

for f in "$SBMTROOT"/src/edu/jhuapl/near/tools/*.java ; do
    f=`basename $f .java`
    echo '#!/bin/bash'                                                        >  $INSTALL_BIN_DIR/$f
    echo 'if [ -z "$SBMTROOT" ]; then'                                        >> $INSTALL_BIN_DIR/$f
        echo '    echo "ERROR: SBMTROOT is undefined!"'                       >> $INSTALL_BIN_DIR/$f
        echo '    exit 1'                                                     >> $INSTALL_BIN_DIR/$f
    echo 'fi'                                                                 >> $INSTALL_BIN_DIR/$f
    echo 'DIR=`dirname "$0"`'                                                 >> $INSTALL_BIN_DIR/$f
    echo 'export PATH="$DIR:$PATH"'                                           >> $INSTALL_BIN_DIR/$f
    echo 'export DYLD_LIBRARY_PATH="$SBMTROOT/lib:$DYLD_LIBRARY_PATH"'          >> $INSTALL_BIN_DIR/$f
    echo 'export LD_LIBRARY_PATH="$SBMTROOT/lib:$LD_LIBRARY_PATH"'              >> $INSTALL_BIN_DIR/$f
    echo 'HEADLESS=""'                                                        >> $INSTALL_BIN_DIR/$f
    echo 'MEMSIZE=""'                                                         >> $INSTALL_BIN_DIR/$f
    echo 'if [ "$(uname)" == "Darwin" ]; then'                                >> $INSTALL_BIN_DIR/$f
    if [ "$f" != "SmallBodyMappingTool" -a "$f" != "SmallBodyMappingToolAPL" ]; then
        echo '    HEADLESS="-Djava.awt.headless=true"'                        >> $INSTALL_BIN_DIR/$f
    fi
    echo '    MEMSIZE=`sysctl hw.memsize | awk '\''{print int($2/1024)}'\''`' >> $INSTALL_BIN_DIR/$f
    echo 'elif [ "$(uname)" == "Linux" ]; then'                               >> $INSTALL_BIN_DIR/$f
    echo '    MEMSIZE=`grep MemTotal /proc/meminfo | awk '\''{print $2}'\''`' >> $INSTALL_BIN_DIR/$f
    echo 'fi'                                                                 >> $INSTALL_BIN_DIR/$f
    echo '/project/nearsdc/software/java/x86_64/latest/bin/java -Xmx${MEMSIZE}K $HEADLESS "-Djava.library.path=$DIR/../lib" -cp "'$CLASSPATH"\" edu.jhuapl.near.tools.$f \$@" >> $INSTALL_BIN_DIR/$f
    chmod +x $INSTALL_BIN_DIR/$f
done


######################################################
# Install Scripts
######################################################
#cp misc/scripts/* $INSTALL_BIN_DIR


######################################################
# Install gravity
######################################################
#cd misc/programs/gravity
#./compile.sh
#cp gravity elevation-slope $INSTALL_BIN_DIR
