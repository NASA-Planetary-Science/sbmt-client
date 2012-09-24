#!/bin/sh

# Use this script to build vtk on a Mac or Linux system. It should
# take 1 argument which is the path to vtk.


MAC_OPTIONS=
if [[ $OSTYPE == darwin* ]]; then
    MAC_OPTIONS=-DCMAKE_OSX_DEPLOYMENT_TARGET:STRING=10.5
    echo "Mac detected"
fi


cmake $MAC_OPTIONS \
    -DCMAKE_BUILD_TYPE:String=Release \
    -DBUILD_SHARED_LIBS:BOOL=ON \
    -DBUILD_TESTING:BOOL=OFF \
    -DVTK_WRAP_JAVA:BOOL=ON \
    $@


make
