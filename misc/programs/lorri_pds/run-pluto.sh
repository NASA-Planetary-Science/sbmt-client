#!/bin/bash

# Script for running the create_info_files program
cd `dirname $0`

# Create a list of all label files
LORRI_DIR=/project/nearsdc/data/NEWHORIZONS/LORRI/images2
LORRI_LABEL_FILES=/project/nearsdc/data/internal/allLorriFiles.txt
find -L $LORRI_DIR -name "*.fit" -type f | sort > $LORRI_LABEL_FILES

runCreateInfoFiles() {
    INFO_DIR=/project/nearsdc/data/NEWHORIZONS/$1/IMAGING/infofiles
    rm -f $INFO_DIR/*
    mkdir -p $INFO_DIR
    OUTPUT_FILE_LIST=/project/nearsdc/data/NEWHORIZONS/$1/IMAGING/imagelist.txt
    CMD="./create_info_files $1 /project/lorri/kernels/nh_auto.mk $LORRI_LABEL_FILES $INFO_DIR $OUTPUT_FILE_LIST"
    $CMD
}

runCreateInfoFiles PLUTO
runCreateInfoFiles CHARON
runCreateInfoFiles HYDRA
runCreateInfoFiles KERBEROS
runCreateInfoFiles NIX
runCreateInfoFiles STYX
