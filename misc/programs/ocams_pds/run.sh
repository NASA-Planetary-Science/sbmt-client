#!/bin/bash

# Script for running the create_info_files program on FITS image files.
# FITS files must reside in folder POLYCAM/images/ and MAPCAM/images.
#
# Usage: run.sh /project/sbmtpipeline/rawdata/osirisrex/
#

cd `dirname $0`

#SPICE kernels for OCAMS
KERNELS=ocams.mk

#SPICE PCK keywords
TARGET=BENNU

#SPICE FK keywords
SCID=ORX
SCFRAME=ORX_SPACECRAFT

#FITS keyword for spacecraft clock string
FITS_SCLK_KEYWORD=SCLK_STR

runCreateInfoFiles() 
{
    INSTR_DIR=$1/$2
    IMAGE_DIR=$INSTR_DIR/images
    
    #rename ".fits" to ".fit"
    for i in $IMAGE_DIR/*.fits
    do
       mv $i ${i%.fits}.fit
    done
    
    #replace colons with underscores in filenames
    for i in $IMAGE_DIR/*:*; 
    do
       mv "$i" "${i//:/_}"
    done
    
    FIT_FILES=$INSTR_DIR/allFitsFiles.txt
    find -L $IMAGE_DIR -name "*.fit" -type f | sort > $FIT_FILES
    
    #File list must be relative path to image directory. It also must be named same as in DatabaseGeneratorSql.RunInfo (imagelist-fullpath.txt):
    cp $FIT_FILES $INSTR_DIR/imagelist-fullpath.txt
    echo $CMD
    DATAPATH=/GASKELL/RQ36_V3/$2
    sed -i "s%$INSTR_DIR%$DATAPATH%g" $INSTR_DIR/imagelist-fullpath.txt

    INFO_DIR=$INSTR_DIR/infofiles
    rm -f $INFO_DIR/*
    mkdir -p $INFO_DIR
    OUTPUT_FILE_LIST=$INSTR_DIR/imagelist.txt
    CMD="./create_info_files $KERNELS $TARGET $SCID $2 $FITS_SCLK_KEYWORD $FIT_FILES $INFO_DIR $OUTPUT_FILE_LIST"
    $CMD
}

#runCreateInfoFiles $1 MAPCAM ORX_OCAMS_MAPCAM
runCreateInfoFiles $1 POLYCAM ORX_OCAMS_POLYCAM

chmod -R g+w $INFO_DIR/*
chmod g+w $FIT_FILES
