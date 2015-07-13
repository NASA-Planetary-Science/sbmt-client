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

cp lib/*.jar $output_dir/mac64/sbmt/lib
cp lib/*.jar $output_dir/linux64/sbmt/lib
cp lib/*.jar $output_dir/win64/sbmt/lib

cp src/edu/jhuapl/near/data/license.txt $output_dir/mac64/sbmt/lib
cp src/edu/jhuapl/near/data/license.txt $output_dir/linux64/sbmt/lib
cp src/edu/jhuapl/near/data/license.txt $output_dir/win64/sbmt/lib

if [ "$version" == "-internal" ]
then
    echo "doing internal"
    cp build/jar/near-apl.jar $output_dir/mac64/sbmt/lib/near.jar
    cp build/jar/near-apl.jar $output_dir/linux64/sbmt/lib/near.jar
    cp build/jar/near-apl.jar $output_dir/win64/sbmt/lib/near.jar
else
    echo "doing public"
    cp build/jar/near.jar $output_dir/mac64/sbmt/lib
    cp build/jar/near.jar $output_dir/linux64/sbmt/lib
    cp build/jar/near.jar $output_dir/win64/sbmt/lib
fi

cp -R $vtk_dir/mac64 $output_dir/mac64/sbmt/lib
cp -R $vtk_dir/linux64 $output_dir/linux64/sbmt/lib
cp -R $vtk_dir/win64 $output_dir/win64/sbmt/lib

cp -R $vtk_dir/jre-mac64 $output_dir/mac64/sbmt
mv $output_dir/mac64/sbmt/jre-mac64 $output_dir/mac64/sbmt/jre

cp -R $vtk_dir/jre-linux64 $output_dir/linux64/sbmt
mv $output_dir/linux64/sbmt/jre-linux64 $output_dir/linux64/sbmt/jre

cp -R $vtk_dir/jre-win64 $output_dir/win64/sbmt
mv $output_dir/win64/sbmt/jre-win64 $output_dir/win64/sbmt/jre

cp $vtk_dir/runsbmt.exe $output_dir/win64/sbmt
chmod +x $output_dir/win64/sbmt/runsbmt.exe


echo -n -e "#!/bin/sh
echo '**********************************'
echo 'CLOSING THIS WINDOW WILL QUIT SBMT'
echo '**********************************'
DIR=\`dirname \"\$0\"\`
DIR=\"\`(cd \"\$DIR\"; pwd)\`\"
export DYLD_LIBRARY_PATH=\"\$DIR/lib/mac64\":\$DYLD_LIBRARY_PATH
MEMSIZE=\`sysctl hw.memsize | awk '{print int(\$2/1024)}'\`
\"\$DIR/jre/bin/java\" -Xmx\${MEMSIZE}K -Djava.library.path=\"\$DIR/lib/mac64\" -Dcom.apple.mrj.application.apple.menu.about.name=\"Small Body Mapping Tool\" -jar \"\$DIR/lib/near.jar\" \$@
" > $output_dir/mac64/sbmt/runsbmt
chmod +x $output_dir/mac64/sbmt/runsbmt

echo -n -e "#!/bin/sh
DIR=\`dirname \"\$0\"\`
DIR=\"\`(cd \"\$DIR\"; pwd)\`\"
export LD_LIBRARY_PATH=\"\$DIR/lib/linux64\":\$LD_LIBRARY_PATH
MEMSIZE=\`grep MemTotal /proc/meminfo | awk '{print \$2}'\`
\"\$DIR/jre/bin/java\" -Xmx\${MEMSIZE}K -Djava.library.path=\"\$DIR/lib/linux64\" -jar \"\$DIR/lib/near.jar\" \$@
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
