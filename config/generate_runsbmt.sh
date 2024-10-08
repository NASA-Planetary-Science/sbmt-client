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
mission=$2

#top_level_dir=`pwd`
top_level_dir=$SBMTROOT


mkdir -p $output_dir/mac64/sbmt/lib
mkdir -p $output_dir/linux64/sbmt/lib
mkdir -p $output_dir/linux64u20/sbmt/lib
mkdir -p $output_dir/win64/sbmt/lib

#echo "top_level_dir=$top_level_dir"
#echo "output_dir=$output_dir"

echo -n -e "#!/bin/sh
DIR=\`dirname \"\$0\"\`
DIR=\"\`(cd \"\$DIR\"; pwd)\`\"
export DYLD_LIBRARY_PATH=\"\$DIR/lib/mac64\":\"\$DIR/lib/gdal/mac64\":\$DYLD_LIBRARY_PATH
export LC_NUMERIC=\"en_US.UTF-8\"
MEMSIZE=\`sysctl hw.memsize | awk '{print int(\$2/1024)}'\`
\"\$DIR/jre/bin/java\" -Xmx\${MEMSIZE}K -Djava.library.path=\"\$DIR/lib/mac64:\$DIR/lib/gdal/mac64\" -Dedu.jhuapl.sbmt.mission="${mission}" --add-exports java.desktop/com.sun.imageio.spi=ALL-UNNAMED --add-exports java.desktop/com.apple.laf=ALL-UNNAMED --add-exports java.desktop/apple.laf=ALL-UNNAMED -jar \"\$DIR/lib/near.jar\" \$@ &
" > $output_dir/mac64/sbmt/runsbmt
chmod +x $output_dir/mac64/sbmt/runsbmt

echo -n -e "#!/bin/sh
DIR=\`dirname \"\$0\"\`
DIR=\"\`(cd \"\$DIR\"; pwd)\`\"
export LD_LIBRARY_PATH=\"\$DIR/lib/linux64\":\"\$DIR/lib/gdal/linux64\":\$LD_LIBRARY_PATH
export LC_NUMERIC=\"en_US.UTF-8\"
MEMSIZE=\`grep MemTotal /proc/meminfo | awk '{print \$2}'\`
\"\$DIR/jre/bin/java\" -Xmx\${MEMSIZE}K -Djava.library.path=\"\$DIR/lib/linux64:\$DIR/lib/gdal/linux64\" -Dedu.jhuapl.sbmt.mission="${mission}" --add-exports java.desktop/com.sun.imageio.spi=ALL-UNNAMED -jar \"\$DIR/lib/near.jar\" \$@ &
" > $output_dir/linux64/sbmt/runsbmt
chmod +x $output_dir/linux64/sbmt/runsbmt

echo -n -e "#!/bin/sh
DIR=\`dirname \"\$0\"\`
DIR=\"\`(cd \"\$DIR\"; pwd)\`\"
export LD_LIBRARY_PATH=\"\$DIR/lib/linux64\":\"\$DIR/lib/linux64u20\":\$LD_LIBRARY_PATH
export LC_NUMERIC=\"en_US.UTF-8\"
MEMSIZE=\`grep MemTotal /proc/meminfo | awk '{print \$2}'\`
\"\$DIR/jre/bin/java\" -Xmx\${MEMSIZE}K -Djava.library.path=\"\$DIR/lib/linux64:\$DIR/lib/linux64u20\" -Dedu.jhuapl.sbmt.mission="${mission}" --add-exports java.desktop/com.sun.imageio.spi=ALL-UNNAMED -jar \"\$DIR/lib/near.jar\" \$@ &
" > $output_dir/linux64u20/sbmt/runsbmt
chmod +x $output_dir/linux64u20/sbmt/runsbmt
