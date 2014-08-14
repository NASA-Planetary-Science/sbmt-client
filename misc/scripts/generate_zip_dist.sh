#!/bin/bash

# This script generates zip file of the compiled code and needed jar
# files and c++ libraries for distribution to users.  It takes
# 3 arguments. The fisrt specifies the folder containing the VTK binary
# libraries. It is assumed this folder contains these 3 subfolders:
#
# mac64   - for 64-bit Mac OS X libraries (Intel based Macs only)
# linux64 - for 64-bit Linux libraries
# win64   - for 64-bit Windows libraries
# jre7    - jre for 64-bit Windows systems
#
# The second argument specifies the folder where the generated zip file is placed.
# The third argument must be either -internal for the APL only version or
# -public for the public version
# The script must be run from the top level folder of the sbmt source tree.

vtk_dir=$1
output_dir=$2
version=$3

top_level_dir=`pwd`

mkdir -p $output_dir/mac64/sbmt/lib
mkdir -p $output_dir/linux64/sbmt/lib
mkdir -p $output_dir/win64/sbmt/lib
mkdir -p $output_dir/win64-with-jre/sbmt/lib

cp lib/*.jar $output_dir/mac64/sbmt/lib
cp lib/*.jar $output_dir/linux64/sbmt/lib
cp lib/*.jar $output_dir/win64/sbmt/lib
cp lib/*.jar $output_dir/win64-with-jre/sbmt/lib

cp src/edu/jhuapl/near/data/license.txt $output_dir/mac64/sbmt/lib
cp src/edu/jhuapl/near/data/license.txt $output_dir/linux64/sbmt/lib
cp src/edu/jhuapl/near/data/license.txt $output_dir/win64/sbmt/lib
cp src/edu/jhuapl/near/data/license.txt $output_dir/win64-with-jre/sbmt/lib

if [ "$version" == "-internal" ]
then
    echo "doing internal"
    cp build/jar/near-apl.jar $output_dir/mac64/sbmt/lib/near.jar
    cp build/jar/near-apl.jar $output_dir/linux64/sbmt/lib/near.jar
    cp build/jar/near-apl.jar $output_dir/win64/sbmt/lib/near.jar
    cp build/jar/near-apl.jar $output_dir/win64-with-jre/sbmt/lib/near.jar
else
    echo "doing public"
    cp build/jar/near.jar $output_dir/mac64/sbmt/lib
    cp build/jar/near.jar $output_dir/linux64/sbmt/lib
    cp build/jar/near.jar $output_dir/win64/sbmt/lib
    cp build/jar/near.jar $output_dir/win64-with-jre/sbmt/lib
fi

cp -R $vtk_dir/mac64 $output_dir/mac64/sbmt/lib
cp -R $vtk_dir/linux64 $output_dir/linux64/sbmt/lib
cp -R $vtk_dir/win64 $output_dir/win64/sbmt/lib
cp -R $vtk_dir/win64 $output_dir/win64-with-jre/sbmt/lib
cp -R $vtk_dir/jre7 $output_dir/win64-with-jre/sbmt

cp $vtk_dir/runsbmt.exe $output_dir/win64/sbmt
chmod +x $output_dir/win64/sbmt/runsbmt.exe
cp $vtk_dir/runsbmt.exe $output_dir/win64-with-jre/sbmt
chmod +x $output_dir/win64-with-jre/sbmt/runsbmt.exe


echo -n -e "#!/bin/sh
echo '**********************************'
echo 'CLOSING THIS WINDOW WILL QUIT SBMT'
echo '**********************************'
DIR=\`dirname \"\$0\"\`
cd \"\$DIR\"
export DYLD_LIBRARY_PATH=lib/mac64:\$DYLD_LIBRARY_PATH
/usr/libexec/java_home -v 1.6 -a x86_64 -exec java -Djava.library.path=lib/mac64 -Dcom.apple.mrj.application.apple.menu.about.name=\"Small Body Mapping Tool\" -jar lib/near.jar \$@
" > $output_dir/mac64/sbmt/runsbmt
chmod +x $output_dir/mac64/sbmt/runsbmt

echo -n -e "#!/bin/sh
DIR=\`dirname \"\$0\"\`
cd \"\$DIR\"
export LD_LIBRARY_PATH=lib/linux64:\$LD_LIBRARY_PATH
java -Djava.library.path=lib/linux64 -jar lib/near.jar \$@
" > $output_dir/linux64/sbmt/runsbmt
chmod +x $output_dir/linux64/sbmt/runsbmt


if [ "$version" == "-internal" ]
then
echo -n -e "replace-with-username
replace-with-password
" > $output_dir/mac64/sbmt/password.txt
cp $output_dir/mac64/sbmt/password.txt $output_dir/linux64/sbmt/

echo -n -e "replace-with-username\r
replace-with-password\r
" > $output_dir/win64/sbmt/password.txt
cp $output_dir/win64/sbmt/password.txt $output_dir/win64-with-jre/sbmt/
fi


cd $output_dir/mac64
version_number=`date +%Y.%m.%d`
zip -q -r sbmt-$version_number-macosx-x64.zip sbmt

cd $output_dir/linux64
version_number=`date +%Y.%m.%d`
zip -q -r sbmt-$version_number-linux-x64.zip sbmt

cd $output_dir/win64
version_number=`date +%Y.%m.%d`
zip -q -r sbmt-$version_number-windows-x64.zip sbmt

cd $output_dir/win64-with-jre
version_number=`date +%Y.%m.%d`
zip -q -r sbmt-$version_number-windows-x64-with-java.zip sbmt


# For Mac internal version add extra tools
if [ "$version" == "-internal" ]
then

build_dir=/tmp/build-sbmt-general
rm -rf $build_dir
mkdir -p $build_dir
cd $build_dir
cmake $top_level_dir/misc/programs/general \
    -DCMAKE_BUILD_TYPE:String=Release \
    -DCMAKE_CXX_FLAGS_RELEASE:String="-O2 -DNDEBUG" \
    -DCMAKE_C_FLAGS_RELEASE:String="-O2 -DNDEBUG"
make

cp gravity $output_dir/mac64/sbmt
cp elevation-slope $output_dir/mac64/sbmt
cp cam-est $output_dir/mac64/sbmt
cp lidar-optimize-track $output_dir/mac64/sbmt
cp lidar-compute-track-stats $output_dir/mac64/sbmt
cp prop $output_dir/mac64/sbmt

cd $top_level_dir
cp misc/scripts/search-lidar $output_dir/mac64/sbmt
cp misc/scripts/convert-mapmaker-cube $output_dir/mac64/sbmt
cp misc/scripts/reproject-lidar-to-fitted-plane $output_dir/mac64/sbmt
cp misc/programs/general/lidar/lidar-opt.py $output_dir/mac64/sbmt

mkdir -p $output_dir/mac64/sbmt/doc
cp misc/programs/general/propagation/README-propagator.txt $output_dir/mac64/sbmt/doc
cp misc/programs/general/gravity/README-gravity.txt $output_dir/mac64/sbmt/doc
cp misc/programs/general/camera_est/README-camera-estimation.txt $output_dir/mac64/sbmt/doc
cp misc/programs/general/lidar/README-lidar-opt.txt $output_dir/mac64/sbmt/doc

cd $output_dir/mac64
version_number=`date +%Y.%m.%d`
zip -q -r sbmt-extras-$version_number-macosx-x64.zip sbmt

fi
