#!/bin/bash

# Script for running the create_info_files program on FITS image files.
#
# FITS files must reside in subfolders POLYCAM/images/ and MAPCAM/images.
# Target body name must be recognized by SPICE (e.g. EROS, EARTH, BENNU).
#
# Usage:
#
# run.sh <path_to_rawdata> <infofile_output_dir> <metakernel> <target_body>
#
# Example: 
#
# ./run.sh /project/sbmtpipeline/rawdata/osirisrex/EarthFlyBy /project/sbmtpipeline/processed/osirisrex/earthflyby /project/sbmtpipeline/rawdata/osirisrex/EarthFlyBy/kernels/spoc-digest-2017-09-26T18_13_09.404Z.mk EARTH
#
 

cd `dirname $0`

#SPICE kernels for OCAMS
KERNELS=$3

#SPICE PCK keywords
TARGET=$4

#SPICE FK keywords
SCID=ORX
SCFRAME=ORX_SPACECRAFT

#FITS keyword for spacecraft clock string
FITS_SCLK_KEYWORD=SCLK_STR

#Input/Output directories
DATADIR=$1
OUTDIR=$2

if (( $# != 4 ))
then
  echo "Usage: run.sh <path_to_rawdata> <infofile_output_dir> <metakernel> <target_body>"
  exit 1
fi

runCreateInfoFiles() 
{
    #Instrument name
    INSTR=$1
    #Instrument frame name (frames kernel)
    NAIFNAME=$2
    #SBMT directory path, relative to /project/sbmt2/data
    SBMTDIR=$3

    IMAGE_DIR=$DATADIR/$INSTR/images

    OUTPUT_DIR=$(echo "$OUTDIR/$INSTR" | tr '[:upper:]' '[:lower:]')
    mkdir -p ${OUTPUT_DIR}
    
    #rename ".fits" to ".fit"
    for i in $IMAGE_DIR/*.fits
    do
       mv $i ${i%.fits}.fit &> /dev/null
    done
    
    #replace colons with underscores in filenames
    for i in $IMAGE_DIR/*:*; 
    do
       mv "$i" "${i//:/_}" &> /dev/null
    done
    
    FIT_FILES=$OUTPUT_DIR/allFitsFiles.txt
    find -L $IMAGE_DIR -name "*.fit" -type f | sort > $FIT_FILES
    
    #File list must be relative path to image directory. It also must be named same as in DatabaseGeneratorSql.RunInfo (imagelist-fullpath.txt):
    cp $FIT_FILES $OUTPUT_DIR/imagelist-fullpath.txt
    DATAPATH=$SBMTDIR/$OUTPUT_LOWER
    sed -i "s%$INSTR_DIR%$DATAPATH%g" $INSTR_DIR/imagelist-fullpath.txt &> /dev/null

    INFO_DIR=$OUTPUT_DIR/infofiles
    rm -f $INFO_DIR/*
    mkdir -p $INFO_DIR
    OUTPUT_FILE_LIST=$OUTPUT_DIR/imagelist.txt
    CMD="./create_info_files $KERNELS $TARGET $SCID $NAIFNAME $FITS_SCLK_KEYWORD $FIT_FILES $INFO_DIR $OUTPUT_FILE_LIST"
    $CMD

    echo "${INSTR} infofiles have been generated. Manually copy fits images from ${IMAGE_DIR} and infofiles from ${INFO_DIR} to sbmt pipeline data subdirectory ${SBMTDIR}"
}

runCreateInfoFiles MAPCAM ORX_OCAMS_MAPCAM /earth/osirisrex
runCreateInfoFiles POLYCAM ORX_OCAMS_POLYCAM /earth/osirisrex
runCreateInfoFiles SAMCAM ORX_OCAMS_SAMCAM  /earth/osirisrex

chmod -R g+w $INFO_DIR/*
chmod g+w $FIT_FILES

#NOW MOVE THE IMAGES AND INFOFILES TO /project/sbmt2/data/earth/osirisrex