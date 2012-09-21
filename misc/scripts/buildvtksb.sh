#!/bin/sh

# Use this script to build vtksb on a Mac or Linux system. vtksb is
# our extension library to vtk. It should take 2 arguments: the first
# is the path to the vtk build folder and the second is the path to
# the vtksb source folder.


MAC_OPTIONS=
if [[ $OSTYPE == darwin* ]]; then
    MAC_OPTIONS=-DCMAKE_OSX_DEPLOYMENT_TARGET:STRING=10.5
    echo "Mac detected"
fi


cmake $MAC_OPTIONS \
    -DVTK_DIR=$1 \
    -DCMAKE_BUILD_TYPE:String=Release \
    -DBUILD_SHARED_LIBS:BOOL=ON \
    -DVTK_WRAP_JAVA:BOOL=ON \
    -DVTKSB_WRAP_JAVA:BOOL=ON \
    -DCMAKE_SKIP_RPATH:BOOL=ON \
    $2


make

# create the jar file
cd java
mkdir vtk
cp vtksb/*.java vtk/
javac -cp $1/bin/vtk.jar vtk/*.java
rm -f vtk/*.java
jar cf vtksb.jar vtk/
