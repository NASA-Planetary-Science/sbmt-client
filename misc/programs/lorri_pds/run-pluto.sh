#!/bin/bash

# Script for running the create_info_files program
cd `dirname $0`

# Tools
MSOPCK=/project/nearsdc/software/spice/cspice/exe/msopck

# add -a to force large pointing updates (> 500 microradians)
CREATE_WCS_KERNELS=/project/lorri/bin/linux/lorri_astro_ck

CREATE_INFO_FILES=./create_info_files

# Create a list of all label files
LORRI_DIR=/project/nearsdc/data/NEWHORIZONS/LORRI/images2

# Files
LORRI_IMAGE_FILES=/project/nearsdc/data/internal/allLorriFiles.txt
LORRI_WCS_IMAGE_FILES=/project/nearsdc/data/internal/allWcsCorrectedLorriFiles.txt
MSOPCK_SETUP_FILE=/project/nearsdc/data/internal/allLorriMsopckSetup.txt
MSOPCK_DATA_FILE=/project/nearsdc/data/internal/allLorriMsopckData.txt
LORRI_METAKERNEL=./nh_spice.mk
AUGMENTED_METAKERNEL=./nh_spice_augmented.mk
WCS_CORRECTED_METAKERNEL=./nh_wcs_spice.mk

# Selects 1x1 images only, will change to include all images
find -L $LORRI_DIR -name "*sci.fit" -type f | sort > $LORRI_IMAGE_FILES

# Selects WCS-corrected files
find -L $LORRI_DIR -name "*pwcs1.fit" -type f | sort > $LORRI_WCS_IMAGE_FILES

# use Howard Taylors's wcs corrected kernel
#
WCS_CORRECTED_CKERNEL=/project/lorri/kernels/nh_lorri_wcs.bc
#
# or
#
# WCS_CORRECTED_CKERNEL=/project/lorri/kernels/nh_wcs_spice.bc
#
# or
#
# build corrected kernel ourselves
#WCS_CORRECTED_CKERNEL=./nh_lorri_wcs.bc
#$CREATE_WCS_KERNELS -l $LORRI_WCS_IMAGE_FILES -s $MSOPCK_SETUP_FILE -q $MSOPCK_DATA_FILE -m $LORRI_METAKERNEL
## NOTE: should append a date suffix to the kernel and metakernel files to archive
#rm -f $WCS_CORRECTED_CKERNEL
#$MSOPCK $MSOPCK_SETUP_FILE $MSOPCK_DATA_FILE $WCS_CORRECTED_CKERNEL

AUGMENTED_METAKERNEL=./nh_spice_augmented.mk

# add the prelim Text PCK for IAU_NIX & IAU_HYDRA to metakernel
cp $LORRI_METAKERNEL $AUGMENTED_METAKERNEL
echo '\begindata' >> $AUGMENTED_METAKERNEL
echo "KERNELS_TO_LOAD += (" >> $AUGMENTED_METAKERNEL
echo "   '/project/nearsdc/nh-sbmt/sbmt/misc/programs/lorri_pds/nh_prelim_v01.tpc'" >> $AUGMENTED_METAKERNEL
echo  ')'  >> $AUGMENTED_METAKERNEL
echo '\begintext'  >> $AUGMENTED_METAKERNEL

# create new kernel with augmented contents as well as the wcs-corrected c-kernel 
cp $AUGMENTED_METAKERNEL $WCS_CORRECTED_METAKERNEL
echo '\begindata' >> $WCS_CORRECTED_METAKERNEL
echo "KERNELS_TO_LOAD += ('$WCS_CORRECTED_CKERNEL'"  >> $WCS_CORRECTED_METAKERNEL
echo  ')'  >> $WCS_CORRECTED_METAKERNEL
echo '\begintext'  >> $WCS_CORRECTED_METAKERNEL

runCreateInfoFiles() {
    INFO_DIR=/project/nearsdc/data/NEWHORIZONS/$1/IMAGING/infofiles
    STANDARD_FILE_LIST=/project/nearsdc/data/NEWHORIZONS/$1/IMAGING/standardimagelist.txt

    rm -f $INFO_DIR/*
    mkdir -p $INFO_DIR
    CMD="$CREATE_INFO_FILES $1 $AUGMENTED_METAKERNEL $LORRI_IMAGE_FILES $INFO_DIR $STANDARD_FILE_LIST"
    $CMD

    WCS_CORRECTED_FILE_LIST=/project/nearsdc/data/NEWHORIZONS/$1/IMAGING/wcscorrectedimagelist.txt
    CMD="$CREATE_INFO_FILES $1 $WCS_CORRECTED_METAKERNEL $LORRI_WCS_IMAGE_FILES $INFO_DIR $WCS_CORRECTED_FILE_LIST"
    echo $CMD
    $CMD

# merge output file lists
    OUTPUT_FILE_LIST=/project/nearsdc/data/NEWHORIZONS/$1/IMAGING/imagelist.txt
    cat $STANDARD_FILE_LIST $WCS_CORRECTED_FILE_LIST | sort > $OUTPUT_FILE_LIST

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
