#!/bin/bash

# Script for running the create_info_files program
cd `dirname $0`

# Create a list of all label files
MVIC_DIR=/project/nearsdc/data/NEWHORIZONS/MVIC/images
MVIC_LABEL_FILES=./allMvicFiles.txt
# MVIC_LABEL_FILES=/project/nearsdc/data/internal/allMvicFiles.txt
# MVIC_LABEL_FILES=goodMvicFiles.txt
find -L $MVIC_DIR -name "*.fit" -type f | sort > $MVIC_LABEL_FILES

runCreateInfoFiles() {
    INFO_DIR=/project/nearsdc/data/NEWHORIZONS/$1/MVIC/infofiles
#    INFO_DIR=./$1/MVIC/infofiles
    rm -f $INFO_DIR/*
    mkdir -p $INFO_DIR
    OUTPUT_FILE_LIST=/project/nearsdc/data/NEWHORIZONS/$1/MVIC/imagelist.txt
#    OUTPUT_FILE_LIST=./$1/MVIC/imagelist.txt
    CMD="./create_info_files $1 nh_auto.mk $MVIC_LABEL_FILES $INFO_DIR $OUTPUT_FILE_LIST"
    $CMD
}

runCreateInfoFiles JUPITER
runCreateInfoFiles EUROPA
runCreateInfoFiles GANYMEDE
runCreateInfoFiles IO
