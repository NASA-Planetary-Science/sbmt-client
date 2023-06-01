#!/bin/bash

# This script generates individual scripts to run each of the tools.

THIS=`basename "$0"`
if [ -z "$SBMTROOT" ]; then
    echo "ERROR in $THIS: SBMTROOT is undefined!"
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
#Need src path for class resources.
CLASSPATH="$CLASSPATH:\$SBMTROOT/src:\$SBMTROOT/lib/*"

JAVA_TOP="/project/nearsdc/software/java/jdk16"

for JAVA_TOOL in "$SBMTROOT"/src/edu/jhuapl/sbmt/tools/*.java ; do
    JAVA_TOOL=`basename $JAVA_TOOL .java`
    echo '#!/bin/bash'                                                                        >  $INSTALL_BIN_DIR/$JAVA_TOOL.sh
    echo '#'                                                                                  >> $INSTALL_BIN_DIR/$JAVA_TOOL.sh
    echo "# This script runs $JAVA_TOOL.java"                                                 >> $INSTALL_BIN_DIR/$JAVA_TOOL.sh
    echo '#'                                                                                  >> $INSTALL_BIN_DIR/$JAVA_TOOL.sh
    echo ''                                                                                   >> $INSTALL_BIN_DIR/$JAVA_TOOL.sh
    echo 'if [ -z "$SBMTROOT" ]; then'                                                        >> $INSTALL_BIN_DIR/$JAVA_TOOL.sh
        echo '    echo "ERROR: SBMTROOT is undefined!"'                                       >> $INSTALL_BIN_DIR/$JAVA_TOOL.sh
        echo '    exit 1'                                                                     >> $INSTALL_BIN_DIR/$JAVA_TOOL.sh
    echo 'fi'                                                                                 >> $INSTALL_BIN_DIR/$JAVA_TOOL.sh
    echo 'DIR=`dirname "$0"`'                                                                 >> $INSTALL_BIN_DIR/$JAVA_TOOL.sh
    echo "JAVA_TOP=$JAVA_TOP"                                                                 >> $INSTALL_BIN_DIR/$JAVA_TOOL.sh
    echo 'export PATH="$DIR:$PATH"'                                                           >> $INSTALL_BIN_DIR/$JAVA_TOOL.sh
    echo 'HEADLESS="-Djava.awt.headless=true"'                                                >> $INSTALL_BIN_DIR/$JAVA_TOOL.sh

    echo 'if [ "$(/bin/uname)" == "Linux" ]; then'                                            >> $INSTALL_BIN_DIR/$JAVA_TOOL.sh
    echo '    $JAVA_TOP/linux64/bin/java -Xmx4G $HEADLESS "-Djava.library.path=$JAVA_TOP/linux64/lib:$DIR/../lib/linux64" -cp "'$CLASSPATH"\" edu.jhuapl.sbmt.tools.$JAVA_TOOL \"\$@\"" >> $INSTALL_BIN_DIR/$JAVA_TOOL.sh
    echo 'else'                                                                               >> $INSTALL_BIN_DIR/$JAVA_TOOL.sh
    echo '    echo "Not set up for anything other than Linux" >&2'                            >> $INSTALL_BIN_DIR/$JAVA_TOOL.sh
    echo 'fi'                                                                                 >> $INSTALL_BIN_DIR/$JAVA_TOOL.sh

    chmod +x $INSTALL_BIN_DIR/$JAVA_TOOL.sh
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
