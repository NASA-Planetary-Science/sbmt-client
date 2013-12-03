#!/bin/sh

# Use this script to build vtk on a Mac or Linux system.

mkdir buildvtk
cd buildvtk

# download vtk
wget -O vtk-5.10.1.tar.gz 'http://www.vtk.org/files/release/5.10/vtk-5.10.1.tar.gz'
tar xzf vtk-5.10.1.tar.gz
mkdir build-vtk
cd build-vtk


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
    ../VTK5.10.1


make


# checkout vtksb from svn
cd ..
svn co http://hardin/svn/sis/projects/near/near_vis/trunk/misc/vtkextras/vtksb
mkdir build-vtksb
cd build-vtksb

cmake $MAC_OPTIONS \
    -DVTK_DIR=../build-vtk \
    -DCMAKE_BUILD_TYPE:String=Release \
    -DBUILD_SHARED_LIBS:BOOL=ON \
    -DVTK_WRAP_JAVA:BOOL=ON \
    -DVTKSB_WRAP_JAVA:BOOL=ON \
    ../vtksb


make

# create the jar file
cd java
mkdir vtk
cp vtksb/*.java vtk/
javac -cp ../../build-vtk/bin/vtk.jar vtk/*.java
rm -f vtk/*.java
jar cf vtksb.jar vtk/
