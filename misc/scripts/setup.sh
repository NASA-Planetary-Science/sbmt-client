# This script must sourced from within this directory (i.e. the
# current directory must be the folder containing this file)


# Setup paths to java
export JAVA_HOME=/project/nearsdc/software/java/x86_64/jdk1.6.0_25
export PATH=$JAVA_HOME/bin:$PATH
export LD_LIBRARY_PATH=$JAVA_HOME/jre/lib/amd64:$JAVA_HOME/jre/lib/amd64/xawt


# Setup virtual display since java has problems in headless mode                                                                              
killall Xvfb
export DISPLAY=":20"
Xvfb $DISPLAY &


# Setup java command including classpath
TOP_DIR=`pwd`/../..
JAVA_COMMAND="java -Djava.library.path=/project/nearsdc/software/vtk_all_platforms/linux64:$JAVA_HOME/jre/lib/amd64/xawt -Dfile.encoding=UTF-8 -classpath $TOP_DIR/bin:$TOP_DIR/lib/vtksb.jar:$TOP_DIR/lib/vtk.jar:$TOP_DIR/lib/fits-1.04.0.jar:$TOP_DIR/lib/jide-oss-2.7.3.jar:$TOP_DIR/lib/joda-time-1.6.jar:$TOP_DIR/lib/mysql-connector-java-5.1.10-bin.jar:$TOP_DIR/lib/commons-io-2.0.jar"

echo -e "$JAVA_COMMAND \n"
