#!/bin/bash

echo "I am in get_ceres_data.sh!!"
exit 1

# Script for getting Ceres data from /project/dawn to the SBMT 
# data pipeline directory. Some filtering is probably needed,
# at least to match the .LBL files to the .FIT files.

DAWN_DATA_DIR=/project/dawn
CERES_DATA_DIR=$DAWN_DATA_DIR/data2
TMP_DIR=/project/sbmtpipeline/rawdata/dawn

find -L $CERES_DATA_DIR -name "*.FIT" -o -name "*.LBL" -not -name "*OPNAV*" -type f | xargs cp -u -t $TMP_DIR/ceres/fc

cd $TMP_DIR
if [ ! -e "$TMP_DIR"/spice -o  ! -h "$TMP_DIR"/spice ] ; then
	echo "$TMP_DIR/spice link does not exist, remaking the link"
	ln -s $CERES_DATA_DIR/spice/ .
else
	echo "$TMP_DIR/spice link exists"
fi
	
if [ ! -e "$TMP_DIR"/NAIF -o  ! -h "$TMP_DIR"/NAIF ] ; then
	echo "$TMP_DIR/NAIF link does not exist, remaking the link"
	ln -s $DAWN_DATA_DIR/NAIF/ .
else
	echo "$TMP_DIR/NAIF link exists"
fi
