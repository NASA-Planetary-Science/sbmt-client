#!/bin/bash

# Script for running the create_info_files program
cd `dirname $0`

# Create a list of all fit files
LEISA_DIR=/project/nearsdc/data/NEWHORIZONS/LEISA/images
LEISA_FIT_FILES=./allLeisaFiles.txt
find -L $LEISA_DIR -name "*.fit" -type f | sort > $LEISA_FIT_FILES

runCreateInfoFiles() {
    INFO_DIR=/project/nearsdc/data/NEWHORIZONS/$1/LEISA/infofiles
    rm -f $INFO_DIR/*
    mkdir -p $INFO_DIR
    OUTPUT_FILE_LIST=/project/nearsdc/data/NEWHORIZONS/$1/LEISA/imagelist.txt
    CMD="./create_info_files $1 nh_auto.mk $LEISA_FIT_FILES $INFO_DIR $OUTPUT_FILE_LIST"
    $CMD
}

runCreateInfoFiles JUPITER
runCreateInfoFiles EUROPA
runCreateInfoFiles GANYMEDE
runCreateInfoFiles IO
runCreateInfoFiles CALLISTO
