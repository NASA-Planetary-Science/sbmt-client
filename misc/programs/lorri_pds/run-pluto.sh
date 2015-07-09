#!/bin/bash

# Script for running the create_info_files program
cd `dirname $0`

# Tools
MSOPCK=/project/nearsdc/software/spice/cspice/exe/msopck
CREATE_WCS_KERNELS=/project/lorri/bin/linux/lorri_astro_ck
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

$CREATE_WCS_KERNELS -l $LORRI_IMAGE_FILES -s $MSOPCK_SETUP_FILE -q $MSOPCK_DATA_FILE -m $SWRI_METAKERNEL

# NOTE: should append a date suffix to the kernel and metakernel files to archive
rm -f $WCS_CORRECTED_KERNEL


$MSOPCK $MSOPCK_SETUP_FILE $MSOPCK_DATA_FILE $WCS_CORRECTED_KERNEL

cp $SWRI_METAKERNEL $WCS_CORRECTED_METAKERNEL

echo '\begindata' >> $WCS_CORRECTED_METAKERNEL
echo "KERNELS_TO_LOAD += ('$WCS_CORRECTED_KERNEL'"  >> $WCS_CORRECTED_METAKERNEL
echo  ')'  >> $WCS_CORRECTED_METAKERNEL
echo '\begintext'  >> $WCS_CORRECTED_METAKERNEL


runCreateInfoFiles() {
    INFO_DIR=/project/nearsdc/data/NEWHORIZONS/$1/IMAGING/infofiles
    OUTPUT_FILE_LIST=/project/nearsdc/data/NEWHORIZONS/$1/IMAGING/imagelist.txt
    rm -f $INFO_DIR/*
    mkdir -p $INFO_DIR
    CMD="./create_info_files $1 $SWRI_METAKERNEL $LORRI_IMAGE_FILES $INFO_DIR $OUTPUT_FILE_LIST"
    $CMD

    WCS_INFO_DIR=/project/nearsdc/data/NEWHORIZONS/$1/IMAGING/wcsinfofiles
    WCS_OUTPUT_FILE_LIST=/project/nearsdc/data/NEWHORIZONS/$1/IMAGING/wcsimagelist.txt
    rm -f $WCS_INFO_DIR/*
    mkdir -p $WCS_INFO_DIR
    CMD="$CREATE_INFO_FILES $1 $WCS_CORRECTED_METAKERNEL $LORRI_IMAGE_FILES $WCS_INFO_DIR $WCS_OUTPUT_FILE_LIST"
    echo $CMD
    $CMD

    # generate database tables
#    pushd /project/nearsdc/data/NEWHORIZONS/$1/IMAGING/
#    find `pwd` -name "*.fit" | sed 's,/project/nearsdc/data,,g' | sort > imagelist-fullpath.txt
#    popd
}

runCreateInfoFiles PLUTO
runCreateInfoFiles CHARON
runCreateInfoFiles HYDRA
runCreateInfoFiles KERBEROS
runCreateInfoFiles NIX
runCreateInfoFiles STYX
