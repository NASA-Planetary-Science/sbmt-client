#!/bin/bash

# This script generates zip file of the compiled code and needed jar
# files and c++ libraries for distribution to users.  It takes a
# single argument specifying the folder containing the VTK binary
# libraries. It is assumed this folder contains these 3 subfolders:
#
# mac64   - for 64-bit Mac OS X libraries (Intel based Macs only)
# linux64 - for 64-bit Linux libraries
# win64   - for 64-bit Windows libraries
# jre6    - jre for 64-bit Windows systems
#
# The generated zip file is placed in $HOME/sbmt
# The script must be run from the top level folder of the sbmt source tree.

vtk_dir=$1
output_dir=$HOME/sbmt

mkdir -p $output_dir/mac64/sbmt/lib
mkdir -p $output_dir/linux64/sbmt/lib
mkdir -p $output_dir/win64/sbmt/lib

cp lib/*.jar $output_dir/mac64/sbmt/lib
cp lib/*.jar $output_dir/linux64/sbmt/lib
cp lib/*.jar $output_dir/win64/sbmt/lib

cp build/jar/near-apl.jar $output_dir/mac64/sbmt/lib
cp build/jar/near-apl.jar $output_dir/linux64/sbmt/lib
cp build/jar/near-apl.jar $output_dir/win64/sbmt/lib

cp -R $vtk_dir/mac64 $output_dir/mac64/sbmt/lib
cp -R $vtk_dir/linux64 $output_dir/linux64/sbmt/lib
cp -R $vtk_dir/win64 $output_dir/win64/sbmt/lib
cp -R $vtk_dir/jre6 $output_dir/win64/sbmt


cd $output_dir/mac64/sbmt

jar_files=`ls lib/*.jar`
jar_files_unix=`echo $jar_files | sed 's/ /:/g'`
jar_files_win=`echo $jar_files | sed 's/ /;/g'`


echo -n -e "#!/bin/sh
cd \`dirname \$0\`
export DYLD_LIBRARY_PATH=lib/mac64:\$DYLD_LIBRARY_PATH
export LD_LIBRARY_PATH=lib/linux64:\$LD_LIBRARY_PATH
java -Djava.library.path=lib/mac64:lib/linux64 -Dcom.apple.mrj.application.apple.menu.about.name=\"Small Body Mapping Tool\" -classpath $jar_files_unix edu.jhuapl.near.SmallBodyMappingToolAPL
" > $output_dir/mac64/sbmt/runsbmt
chmod +x $output_dir/mac64/sbmt/runsbmt
cp $output_dir/mac64/sbmt/runsbmt $output_dir/linux64/sbmt/

echo -n -e "replace-with-username
replace-with-password
" > $output_dir/mac64/sbmt/password.txt
cp $output_dir/mac64/sbmt/password.txt $output_dir/linux64/sbmt/


echo -n -e "@echo off\r
set PATH=lib\\\\win64;%PATH%\r
jre6\\\\bin\\\\java -Djava.library.path=lib/win64 -Dsun.java2d.noddraw=true -classpath $jar_files_win edu.jhuapl.near.SmallBodyMappingToolAPL\r" > $output_dir/win64/sbmt/runsbmt.bat
chmod +x $output_dir/win64/sbmt/runsbmt.bat

echo -n -e "replace-with-username\r
replace-with-password\r
" > $output_dir/win64/sbmt/password.txt


cd $output_dir/mac64
version=`date +%Y.%m.%d`
zip -q -r sbmt-$version-macosx-x64.zip sbmt

cd $output_dir/linux64
version=`date +%Y.%m.%d`
zip -q -r sbmt-$version-linux-x64.zip sbmt

cd $output_dir/win64
version=`date +%Y.%m.%d`
zip -q -r sbmt-$version-windows-x64.zip sbmt
