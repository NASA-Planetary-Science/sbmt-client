#!/bin/bash

# This script generates zip file of the compiled code and needed jar
# files and c++ libraries for distribution to users.  It takes a
# single argument specifying the folder containing the VTK binary
# libraries. It is assumed this folder contains these 4 subfolders:
#
# linux32 - for 32-bit Linux libraries
# linux64 - for 64-bit Linux libraries
# win32   - for 32-bit Windows libraries
# mac64   - for 64-bit Mac OS X libraries (Intel based Macs only)
#
# The generated zip file is placed in $HOME/sbmt
# The script must be run from the top level folder of the sbmt source tree.

vtk_dir=$1
output_dir=$HOME/sbmt

mkdir -p $output_dir/sbmt/lib

cp lib/*.jar $output_dir/sbmt/lib
cp build/jar/near-apl.jar $output_dir/sbmt/lib
cp -R $vtk_dir/linux32 $output_dir/sbmt/lib
cp -R $vtk_dir/linux64 $output_dir/sbmt/lib
cp -R $vtk_dir/win32 $output_dir/sbmt/lib
cp -R $vtk_dir/mac64 $output_dir/sbmt/lib

cd $output_dir/sbmt

jar_files=`ls lib/*.jar`
jar_files_unix=`echo $jar_files | sed 's/ /:/g'`
jar_files_win=`echo $jar_files | sed 's/ /;/g'`

echo -n -e "#!/bin/sh
cd \`dirname \$0\`
MACHINE=\`uname -m\`
if [ \"\${MACHINE}\" = \"x86_64\" ]; then
    VTK_DIR=lib/mac64:lib/linux64
else
    VTK_DIR=lib/mac64:lib/linux32
fi
java -Djava.library.path=\${VTK_DIR} -Dcom.apple.mrj.application.apple.menu.about.name=\"Small Body Mapping Tool\" -classpath $jar_files_unix edu.jhuapl.near.SmallBodyMappingToolAPL
" > $output_dir/sbmt/runsbmt.sh
chmod +x $output_dir/sbmt/runsbmt.sh

echo -n -e "@echo off\r
start javaw -Djava.library.path=lib/win32 -Dsun.java2d.noddraw=true -classpath $jar_files_win edu.jhuapl.near.SmallBodyMappingToolAPL\r
" > $output_dir/sbmt/runsbmt.bat
chmod +x $output_dir/sbmt/runsbmt.bat


cd $output_dir
version=`date +%F`
zip -q -r sbmt-$version.zip sbmt
