# Runs an SBMT standalone tool (e.g. DatabaseGeneratorSql) 
#
# Example:
#
# run-on-linux.sh edu.jhuapl.near.tools.DatabaseGeneratorSql
#
# Requires the $SBMTROOT to be defined for a repository that has a "build" directory,
# that has been built using "make release"


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
