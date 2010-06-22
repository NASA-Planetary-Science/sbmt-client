#!/bin/sh

CK_KERNEL_DIR=/disks/dg007/near/kahneg1/eros_kernels/EROS/GEOM/data/CK
NEAR_DDR_CMD=/disks/dg007/near/kahneg1/code/code/near_ddr

# Read in the list of all 'iofdbl' and 'cifdbl' directories
RAW_DIRS=`cat /disks/dg007/near/kahneg1/data/internal/allMsiDir.txt`

LOG_FILE=allMsiDir_log_`date +'%F-%k%M%S'`

date > $LOG_FILE

echo $RAW_DIRS >> $LOG_FILE

for path in $RAW_DIRS 
do
    echo $path >> $LOG_FILE
	
    # Now from the path extract the date of the data in this folder
    # and use it to figure out the correct CK kernel to load
    TMP=`dirname $path`
    DAY=`basename $TMP`
    TMP=`dirname $TMP`
    YEAR=`basename $TMP`
    
    echo $YEAR$DAY >> $LOG_FILE
    
    CK_KERNEL=`ls $CK_KERNEL_DIR/*$YEAR$DAY*.BC`
  
    if test ${#CK_KERNEL} -gt 0
    then
        
        echo $CK_KERNEL >> $LOG_FILE
        LBL_FILES=`find $path -name "*.LBL" -not -name "*DDR*" -type l | sort`

        for lblfile in $LBL_FILES
	    do
	        echo $NEAR_DDR_CMD $CK_KERNEL $lblfile >> $LOG_FILE
	        $NEAR_DDR_CMD $CK_KERNEL $lblfile >> $LOG_FILE 2>&1
        done
    fi
done

date >> $LOG_FILE
