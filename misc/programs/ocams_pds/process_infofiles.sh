#!/bin/bash

###############################################################################
#
# Process infofiles for OSIRIS-REx imaging instruments and generate imagelist.txt
# for fixed list queries and imagelist-fullpath.txt for DatabaseGeneratorSQL.
#
# Requirements (must be installed on server machine): 
#    HEASOFT
#    SPICE
#
# Usage:
#
# process_infofiles.sh <image_dir> <camera_frame_name> <infofile_output_dir> <target_body_name> <sbmt2_relative_path>
#
# Example:
#
# ./process_infofiles.sh /project/sbmtpipeline/rawdata/osirisrex/EarthFlyBy/MAPCAM/images MAPCAM /project/sbmtpipeline/processed/osirisrex/earthflyby EARTH earth/osirisrex /project/sbmtpipeline/rawdata/osirisrex/EarthFlyBy/kernel_files
#
##############################################################################

#############################################
# Read inputs and setup directories         #
#############################################

#Check arguments
if (( $# != 6 ))
then
  echo "Usage: process_infofiles.sh <image_dir> <camera_name> <infofile_output_dir> <target_body_name> <sbmt2_relative_path> <metakernel_path>"
#  echo "Example: ./process_infofiles.sh /project/sbmtpipeline/rawdata/osirisrex/EarthFlyBy/MAPCAM/images MAPCAM /project/sbmtpipeline/processed/osirisrex/earthflyby EARTH earth/osirisrex /project/sbmtpipeline/rawdata/osirisrex/EarthFlyBy/kernel_files
  exit 1
fi

CAMERAUPPER=$(echo "$2" | tr '[:lower:]' '[:upper:]')
CAMERALOWER=$(echo "$2" | tr '[:upper:]' '[:lower:]')

#SPICE PCK keywords
TARGET=$4

#Path to images on SBMT server
SBMTDIR=$5

#Metakernel path
MKPATH=$6

#SPICE FK keywords 
SCID=ORX
SCFRAME=ORX_SPACECRAFT
CAMERAFRAME=ORX_OCAMS_$CAMERAUPPER

#Input/Output directories
IMAGEDIR=$1
OUTDIR=$3/$CAMERALOWER
INFODIR=$OUTDIR/infofiles

#Make output directories
rm -f $INFODIR/*
mkdir -p "$INFODIR"

#For fixed list queries
OUTPUT_FILE_LIST=$OUTDIR/imagelist.txt
rm $OUTPUT_FILE_LIST 

#############################################
# OCAMS fits files                          #
#
# TBD: NEED TO SEE IF FILE ALREADY EXISTS WITH A DIFFERENT VERSION NUMBER
#
#############################################

#OCAMS FITS keyword for spacecraft clock string 
FITS_SCLK_KEYWORD=SCLK_STR

#rename ".fits" to ".fit" 
for i in $IMAGEDIR/*.fits
do
   mv $i ${i%.fits}.fit &> /dev/null
done

#replace colons with underscores in filenames
for i in $IMAGE_DIR/*:*;
do
   mv "$i" "${i//:/_}" &> /dev/null
done

#File list, I don't think this is used 
#FIT_FILES=$OUTDIR/allFitsFiles.txt
#find -L $IMAGEDIR -name "*.fit" -type f | sort > $FIT_FILES

#File list must contain relative path to image directory. It also must be
#named same as in DatabaseGeneratorSql.RunInfo (imagelist-fullpath.txt):
#cp $FIT_FILES $OUTDIR/imagelist-fullpath.txt
DATAPATH=$SBMTDIR/$CAMERALOWER
sed -i "s%$OUTDIR%$DATAPATH%g" $OUTDIR/imagelist-fullpath.txt &> /dev/null

#Loop through the images and process one by one
for IMAGE in ${IMAGEDIR}/*.fit*; do
    echo Current image is ${IMAGE}

    #Keyword that contains the metakernel name
    MK_KEYWORD=META_KER

    #Use heasoft's fkeyprint to get the contents of the
    #keyword from the kernel file. This involves passing 
    #parameters on the command line, which is taken care of
    #by this command:
    MK_STR=`echo "${IMAGE}
    ${MK_KEYWORD}" | fkeyprint`

    #Dump to screen
    #echo ${MK_STR}

    #Parse out the metakernel name, which is in quotes in
    #the string returned from fkeyprint
    eval METAKERNEL=$(echo ${MK_STR} | grep -o \'.*\')
    METAKERNEL=$MKPATH/$METAKERNEL
    echo Metakernel is ${METAKERNEL}

    CURRENTIMAGE=$OUTDIR/imageToProcess.txt
    rm $CURRENTIMAGE
    echo $IMAGE > $CURRENTIMAGE

#    #File list must contain relative path to image directory. It also must be 
#    #named same as in DatabaseGeneratorSql.RunInfo (imagelist-fullpath.txt):
#    cp $FIT_FILES $OUTDIR/imagelist-fullpath.txt
#    DATAPATH=$SBMTDIR/$CAMERALOWER
#    sed -i "s%$OUTDIR%$DATAPATH%g" $OUTDIR/imagelist-fullpath.txt &> /dev/null

    TMP_OUTPUT_FILE_LIST=$OUTDIR/temp.txt

    CMD="./create_info_files $METAKERNEL $TARGET $SCID $CAMERAFRAME $FITS_SCLK_KEYWORD $CURRENTIMAGE $INFODIR $TMP_OUTPUT_FILE_LIST"
    $CMD

    #Append processed image to imagelist.txt
    cat "$TMP_OUTPUT_FILE_LIST" >> "$OUTPUT_FILE_LIST"

#    echo "${CAMERALOWER} infofile has been generated for ${IMAGE}."
#    echo "Manually copy fits images from ${IMAGEDIR} and infofiles from ${INFODIR} to sbmt pipeline data subdirectory ${SBMTDIR}"
done

#chmod -R g+w $OUTDIR/*
#chmod g+w $IMAGEDIR/*

#mkdir -p /project/sbmt2/data/earth/osirisrex/mapcam/images
#mkdir -p /project/sbmt2/data/earth/osirisrex/mapcam/infofiles
#cp /project/sbmtpipeline/rawdata/osirisrex/EarthFlyBy/MAPCAM/images/*.fit /project/sbmt2/data/earth/osirisrex/mapcam/images/
#cp /project/sbmtpipeline/processed/osirisrex/earthflyby/mapcam/imagelist.txt  /project/sbmt2/data/earth/osirisrex/mapcam/