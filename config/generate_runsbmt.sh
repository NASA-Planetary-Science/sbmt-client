#!/bin/bash


#echo $# arguments
if [ -z "$SBMTROOT" ]
then
	echo "ERROR: SBMTROOT is undefined!"
	exit 1
fi
if [ "$#" -lt 2 ]
then
	echo "ERROR: must provide output directory and version parameters"
	exit 1
fi

output_dir=$1
version=$2
mission=$3

#top_level_dir=`pwd`
top_level_dir=$SBMTROOT


mkdir -p $output_dir/mac64/sbmt/lib
mkdir -p $output_dir/linux64/sbmt/lib
mkdir -p $output_dir/win64/sbmt/lib

#echo "top_level_dir=$top_level_dir"
#echo "output_dir=$output_dir"
#echo "version=$version"


echo -n -e "#!/bin/sh
echo '**********************************'
echo 'CLOSING THIS WINDOW WILL QUIT SBMT'
echo '**********************************'
DIR=\`dirname \"\$0\"\`
DIR=\"\`(cd \"\$DIR\"; pwd)\`\"
export DYLD_LIBRARY_PATH=\"\$DIR/lib/mac64\":\$DYLD_LIBRARY_PATH
MEMSIZE=\`sysctl hw.memsize | awk '{print int(\$2/1024)}'\`
\"\$DIR/jre/bin/java\" -Xmx\${MEMSIZE}K -Djava.library.path=\"\$DIR/lib/mac64\" -Dcom.apple.mrj.application.apple.menu.about.name=\"Small Body Mapping Tool\" -Dedu.jhuapl.sbmt.mission=$mission -jar \"\$DIR/lib/near.jar\" \$@
" > $output_dir/mac64/sbmt/runsbmt
chmod +x $output_dir/mac64/sbmt/runsbmt

echo -n -e "#!/bin/sh
DIR=\`dirname \"\$0\"\`
DIR=\"\`(cd \"\$DIR\"; pwd)\`\"
export LD_LIBRARY_PATH=\"\$DIR/lib/linux64\":\$LD_LIBRARY_PATH
MEMSIZE=\`grep MemTotal /proc/meminfo | awk '{print \$2}'\`
\"\$DIR/jre/bin/java\" -Xmx\${MEMSIZE}K -Djava.library.path=\"\$DIR/lib/linux64\" -Dedu.jhuapl.sbmt.mission=$mission  -jar \"\$DIR/lib/near.jar\" \$@
" > $output_dir/linux64/sbmt/runsbmt
chmod +x $output_dir/linux64/sbmt/runsbmt


if [ "$version" == "internal" ]
then
echo -n -e "replace-with-username
replace-with-password
" > $output_dir/mac64/sbmt/password.txt
cp $output_dir/mac64/sbmt/password.txt $output_dir/linux64/sbmt/

echo -n -e "replace-with-username\r
replace-with-password\r
" > $output_dir/win64/sbmt/password.txt
fi

