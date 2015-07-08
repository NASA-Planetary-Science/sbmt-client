#!/bin/bash

# Script for running the create_info_files program
cd `dirname $0`

# Tools
MSOPCK=/project/nearsdc/software/spice/cspice/exe/msopck
CREATE_WCS_KERNELS=./create_wcs_kernels
CREATE_INFO_FILES=./create_info_files

# Create a list of all label files
LORRI_DIR=/project/nearsdc/data/NEWHORIZONS/LORRI/images2

# Files
LORRI_IMAGE_FILES=/project/nearsdc/data/internal/allLorriFiles.txt
MSOPCK_SETUP_FILE=/project/nearsdc/data/internal/allLorriMsopckSetup.txt
MSOPCK_DATA_FILE=/project/nearsdc/data/internal/allLorriMsopckData.txt
WCS_CORRECTED_KERNEL=./nh_wcs_spice.bc
SWRI_METAKERNEL=./nh_spice.mk

WCS_CORRECTED_METAKERNEL=./nh_wcs_spice.mk


find -L $LORRI_DIR -name "*.fit" -type f | sort > $LORRI_IMAGE_FILES

$CREATE_WCS_KERNELS -imagelist $LORRI_IMAGE_FILES -setupfile $MSOPCK_SETUP_FILE -datafile $MSOPCK_DATA_FILE -spicekernel $SWRI_METAKERNEL

# NOTE: should append a date suffix to the kernel and metakernel files to archive
rm -f $WCS_CORRECTED_KERNEL


$MSOPCK $MSOPCK_SETUP_FILE $MSOPCK_DATA_FILE $WCS_CORRECTED_KERNEL

cp $SWRI_METAKERNEL $WCS_CORRECTED_KERNEL

echo '

echo '\begindata' >> $WCS_CORRECTED_KERNEL
echo "KERNELS_TO_LOAD += ('$WCS_CORRECTED_KERNEL'"  >> $WCS_CORRECTED_KERNEL
echo  ')'  >> $WCS_CORRECTED_KERNEL
echo '\begintext'  >> $WCS_CORRECTED_KERNEL


runCreateInfoFiles() {
    INFO_DIR=/project/nearsdc/data/NEWHORIZONS/$1/IMAGING/infofiles
    rm -f $INFO_DIR/*
    mkdir -p $INFO_DIR
    OUTPUT_FILE_LIST=/project/nearsdc/data/NEWHORIZONS/$1/IMAGING/imagelist.txt
    CMD="./create_info_files $1 $SWRI_METAKERNEL $LORRI_IMAGE_FILES $INFO_DIR $OUTPUT_FILE_LIST"
    $CMD

    WCS_INFO_DIR=/project/nearsdc/data/NEWHORIZONS/$1/IMAGING/wcsinfofiles
    rm -f $WCS_INFO_DIR/*
    mkdir -p $WCS_INFO_DIR
    CMD="$CREATE_INFO_FILES $1 $WCS_CORRECTED_METAKERNEL $LORRI_IMAGE_FILES $INFO_DIR $OUTPUT_FILE_LIST"
    $CMD

}

runCreateInfoFiles PLUTO
runCreateInfoFiles CHARON
runCreateInfoFiles HYDRA
runCreateInfoFiles KERBEROS
runCreateInfoFiles NIX
runCreateInfoFiles STYX
