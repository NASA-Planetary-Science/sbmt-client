#!/bin/bash


#echo $# arguments
if [ -z "$SBMTROOT" ]
then
	echo "ERROR: SBMTROOT is undefined!"
	exit 1
fi
if [ "$#" -lt 1 ]
then
	echo "ERROR: must provide output directory"
	exit 1
fi

output_dir=$1

#top_level_dir=`pwd`
top_level_dir=$SBMTROOT


mkdir -p $output_dir/mac64/sbmt/lib
mkdir -p $output_dir/linux64/sbmt/lib
mkdir -p $output_dir/win64/sbmt/lib

#echo "top_level_dir=$top_level_dir"
#echo "output_dir=$output_dir"

echo -n -e "#!/bin/sh
DIR=\`dirname \"\$0\"\`
DIR=\"\`(cd \"\$DIR\"; pwd)\`\"
export DYLD_LIBRARY_PATH=\"\$DIR/lib/mac64\":\$DYLD_LIBRARY_PATH
export LC_NUMERIC="en_US.UTF-8"
MEMSIZE=\`sysctl hw.memsize | awk '{print int(\$2/1024)}'\`
\"\$DIR/jre/bin/java\" -Xmx\${MEMSIZE}K -Djava.library.path=\"\$DIR/lib/mac64\" -jar \"\$DIR/lib/near.jar\" \$@ &
" > $output_dir/mac64/sbmt/runsbmt
chmod +x $output_dir/mac64/sbmt/runsbmt

echo -n -e "#!/bin/sh
DIR=\`dirname \"\$0\"\`
DIR=\"\`(cd \"\$DIR\"; pwd)\`\"
export LD_LIBRARY_PATH=\"\$DIR/lib/linux64\":\$LD_LIBRARY_PATH
export LC_NUMERIC="en_US.UTF-8"
MEMSIZE=\`grep MemTotal /proc/meminfo | awk '{print \$2}'\`
\"\$DIR/jre/bin/java\" -Xmx\${MEMSIZE}K -Djava.library.path=\"\$DIR/lib/linux64\" -jar \"\$DIR/lib/near.jar\" \$@ &
" > $output_dir/linux64/sbmt/runsbmt
chmod +x $output_dir/linux64/sbmt/runsbmt
