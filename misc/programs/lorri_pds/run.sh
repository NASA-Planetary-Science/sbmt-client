#!/bin/bash

# Script for running the create_info_files program
cd `dirname $0`

# Create a list of all label files
#LORRI_DIR=/project/nearsdc/data/NEWHORIZONS/LORRI/images
#LORRI_LABEL_FILES=/project/nearsdc/data/internal/allLorriFiles.txt
#find -L $LORRI_DIR -name "*.fit" -type f | sort > $LORRI_LABEL_FILES
LORRI_LABEL_FILES=goodLorriFiles.txt

runCreateInfoFiles() {
    INFO_DIR=/project/nearsdc/data/NEWHORIZONS/$1/IMAGING/infofiles
    rm -f $INFO_DIR/*
    mkdir -p $INFO_DIR
    OUTPUT_FILE_LIST=/project/nearsdc/data/NEWHORIZONS/$1/IMAGING/imagelist.txt
    CMD="./create_info_files $1 nh_auto.mk $LORRI_LABEL_FILES $INFO_DIR $OUTPUT_FILE_LIST"
    $CMD
}

runCreateInfoFiles JUPITER
runCreateInfoFiles CALLISTO
runCreateInfoFiles ELARA
runCreateInfoFiles EUROPA
runCreateInfoFiles GANYMEDE
runCreateInfoFiles HIMALIA
runCreateInfoFiles IO
