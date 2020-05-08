#!/bin/bash

#
# Script for running the create_info_files program on FITS image files.
#
# FITS files must reside in subfolders <path_to_rawdata>/<camera-name-informal>/images/ 
# Target body name must be recognized by SPICE (e.g. EROS, EARTH, BENNU).
#
# Usage:
# generateInfoFiles.sh <path_to_rawdata> <infofile_output_dir> <metakernel> <target_body_uppercase> <sc_id> <sc_frame> <camera-name-informal> <camera-frame-kernel-name-uppercase>
#
# Examples: 
#
# ./generateInfoFile.sh $srcTop/shared $destTop/shared $latestKernel $bodyNameCaps ORX ORX polycam ORX_OCAMS_POLYCAM
# ./run.sh /project/sbmtpipeline/rawdata2/planets/earth/ /project/sbmtpipeline/processed2/planets/earth /project/sbmtpipeline/rawdata2/planets/earth/kernel_files/spoc-digest-2017-10-05T22_05_46.366Z.mk EARTH ORX ORX polycam ORX_OCAMS_POLYCAM

export PATH=${PATH}:/project/sbmtpipeline/software/heasoft/bin/

cd `dirname $0`

#Input/Output directories
DATADIR=$1
OUTDIR=$2

#SPICE Metakernel
KERNELS=$3

#Target body SPICE keyword from the PCK
TARGET=$4

#SPICE FK keywords
SCID=$5
SCFRAME=$6

#FITS keyword for spacecraft clock string
FITS_SCLK_KEYWORD=DATE-OBS


# $1 = informal camera name (eg. onc, tir, polycam) $2 = formal camera name (eg. HAYABUSA2_ONC-T, HAYABUSA2_TIR-S, ORX_OCAMS_POLYCAM),
runCreateInfoFiles()
{
    #Instrument name
    INSTR=$1
    #Instrument frame name (frames kernel)
    NAIFNAME=$2

    IMAGE_DIR=$DATADIR/$INSTR/images

    OUTPUT_DIR=$(echo "$OUTDIR/$INSTR" | tr '[:upper:]' '[:lower:]')
    mkdir -p ${OUTPUT_DIR}

    rename ".fits" to ".fit"
    for i in $IMAGE_DIR/*.fits
    do
       mv $i ${i%.fits}.fit &> /dev/null
    done

    #replace colons with underscores in filenames
    for i in $IMAGE_DIR/*:*;
    do
       mv "$i" "${i//:/_}" &> /dev/null
    done

    #Create list of all files matching .fit extension
    FIT_FILES=$OUTPUT_DIR/allFitsFiles2.txt
    find -L $IMAGE_DIR -name "*.fit" -type f | sort > $FIT_FILES
    echo Image Dir: $IMAGE_DIR

    echo "Copying files to full path info list"
    cp $FIT_FILES $OUTPUT_DIR/imagelist-fullpath-info.txt
    BODY=${TARGET,,}
    sed -i "s:^.*"$BODY":\/"$BODY":" $OUTPUT_DIR/imagelist-fullpath-info.txt

    #Setup parameters for use in create_info_files
    INFO_DIR=$OUTPUT_DIR/infofiles
    rm -f $INFO_DIR/*
    mkdir -p $INFO_DIR
    OUTPUT_FILE_LIST=$OUTPUT_DIR/imagelist-info.txt

    echo "Running Create Info Files with command:"
    # runs create_info_files, located in /project/sbmt2/sbmt/scripts, create_info_files is C++ code compiled from code in /project/sbmt2/sbmt/scripts/make-create-info-files
    CMD="./create_info_files $KERNELS $TARGET $SCID $NAIFNAME $FITS_SCLK_KEYWORD $FIT_FILES $INFO_DIR $OUTPUT_FILE_LIST"
    echo $CMD
    $CMD

    echo "${INSTR} infofiles have been generated. Manually copy fits images from ${IMAGE_DIR} and infofiles from ${INFO_DIR} to appropriate destination"
}

if (( $# != 8 ))
then
  echo "Usage: run.sh <path_to_rawdata> <infofile_output_dir> <metakernel> <target_body> <sc_id> <sc_frame> <instrument_name> <instrument_frame_name>"
  exit 1
fi


# $5 = informal camera name (eg. onc, tir, polycam) $6 = formal camera name (eg. HAYABUSA2_ONC-T, HAYABUSA2_TIR-S, ORX_OCAMS_POLYCAM)
runCreateInfoFiles $7 $8 

#echo $INFO_DIR
#echo $FIT_FILES

chmod -R g+w $INFO_DIR/*
chmod g+w $FIT_FILES

