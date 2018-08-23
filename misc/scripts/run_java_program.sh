#!/bin/bash

# Script meant to run java program from within checkout version of
# code. Can be run from any folder but this script cannot be moved to
# another location. ANT must first be used to compile to java code and
# create the near-apl.jar file. The VTK compiled shared libraries must
# be located at /project/nearsdc/software/vtk_all_platforms/current
#
# Example:
#
# run_java_program.sh edu.jhuapl.near.tools.SmallBodyMappingTool


VTK_LIB_DIR=$SBMTROOT/build/dist/internal/linux64/sbmt/lib/linux64

export DYLD_LIBRARY_PATH="$VTK_LIB_DIR:$DYLD_LIBRARY_PATH"
export LD_LIBRARY_PATH="$VTK_LIB_DIR:$LD_LIBRARY_PATH"

DIR=`dirname "$0"`
TOP_DIR=$SBMTROOT

JAR_FILES="$TOP_DIR/build/jar/near-apl.jar"
for f in $TOP_DIR/lib/*.jar ; do
    JAR_FILES="$JAR_FILES:$f"
done

java "-Djava.library.path=$VTK_LIB_DIR" -cp "$JAR_FILES" $@
