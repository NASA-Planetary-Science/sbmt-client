#!/bin/bash

# Run this script from the top level folder to build all java tools
# and package them in an easy to use way. Include at least one
# argument (can be anything) to also build the 'general' library.

set -e
set -o pipefail

INSTALL_DIR=`pwd`/build/sbmt-extras
INSTALL_BIN_DIR=$INSTALL_DIR/bin
INSTALL_LIB_DIR=$INSTALL_DIR/lib

mkdir -p $INSTALL_DIR
mkdir -p $INSTALL_BIN_DIR
mkdir -p $INSTALL_LIB_DIR

######################################################
# Install Java tools
######################################################
ant jarapl
cp build/jar/near-apl.jar $INSTALL_LIB_DIR/near.jar
cp lib/*.jar $INSTALL_LIB_DIR
rsync -av /project/nearsdc/software/vtk_all_platforms/mac64/ $INSTALL_LIB_DIR
rsync -av /project/nearsdc/software/vtk_all_platforms/linux64/ $INSTALL_LIB_DIR

# Generate Run Scripts
CLASSPATH='$DIR/../lib/near.jar'
for f in lib/*.jar ; do
    f=`basename $f`
    CLASSPATH="$CLASSPATH:\$DIR/../lib/$f"
done

for f in src/edu/jhuapl/near/tools/*.java ; do
    f=`basename $f .java`
    echo '#!/bin/bash'                                                        >  $INSTALL_BIN_DIR/$f
    echo 'DIR=`dirname "$0"`'                                                 >> $INSTALL_BIN_DIR/$f
    echo 'export PATH="$DIR:$PATH"'                                           >> $INSTALL_BIN_DIR/$f
    echo 'export DYLD_LIBRARY_PATH="$DIR/../lib:$DYLD_LIBRARY_PATH"'          >> $INSTALL_BIN_DIR/$f
    echo 'export LD_LIBRARY_PATH="$DIR/../lib:$LD_LIBRARY_PATH"'              >> $INSTALL_BIN_DIR/$f
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
    echo 'java -Xmx${MEMSIZE}K $HEADLESS "-Djava.library.path=$DIR/../lib" -cp "'$CLASSPATH"\" edu.jhuapl.near.tools.$f \$@" >> $INSTALL_BIN_DIR/$f
    chmod +x $INSTALL_BIN_DIR/$f
done

######################################################
# Install Scripts
######################################################
cp misc/scripts/* $INSTALL_BIN_DIR


if [ "$#" -gt 0 ]; then
######################################################
# Install general library
######################################################
    build_dir=`pwd`/build/build-sbmt-general
    src_dir=`pwd`/misc/programs/general
    mkdir -p $build_dir
    (
        cd $build_dir
        cmake $src_dir \
            -DCMAKE_BUILD_TYPE:String=Release \
            -DCMAKE_CXX_FLAGS_RELEASE:String="-O2 -DNDEBUG" \
            -DCMAKE_C_FLAGS_RELEASE:String="-O2 -DNDEBUG"
        make

        for f in * ; do
            if [[ -f "$f" && -x "$f" ]]; then
                cp $f $INSTALL_BIN_DIR
            fi
        done
    )

    cp $src_dir/lidar/lidar-opt.py $INSTALL_BIN_DIR
    cp $src_dir/lidar/lidar_opt_tile_eros.py $INSTALL_BIN_DIR
fi
