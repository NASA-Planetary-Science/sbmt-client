#!/bin/bash

# Script for running the create_info_files program on FITS image files
# FITS files must reside in the instrument folder
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

#DATA_DIR=/project/nearsdc/data/GASKELL/RQ36_V3

runCreateInfoFiles() 
{
    IMAGE_DIR=$1/images
    
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
    
    FIT_FILES=$1/allFitsFiles.txt
    find -L $IMAGE_DIR -name "*.fit" -type f | sort > $FIT_FILES

    INFO_DIR=$1/infofiles
    rm -f $INFO_DIR/*
    mkdir -p $INFO_DIR
    OUTPUT_FILE_LIST=$1/imagelist.txt
    CMD="./create_info_files $KERNELS $TARGET $SCID $2 $FITS_SCLK_KEYWORD $FIT_FILES $INFO_DIR $OUTPUT_FILE_LIST"
    $CMD
}

runCreateInfoFiles MAPCAM ORX_OCAMS_MAPCAM
runCreateInfoFiles POLYCAM ORX_OCAMS_POLYCAM

chmod -R g+w $INFO_DIR/*
chmod g+w $FIT_FILES
