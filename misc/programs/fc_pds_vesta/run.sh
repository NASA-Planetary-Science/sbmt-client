#!/bin/bash

# Script for running the create_info_files program
cd `dirname $0`

# Create a list of all fit files
FC_DIR=/project/nearsdc/data-apl/VESTA/FC/images
FC_FIT_FILES=/project/nearsdc/data/internal/allFcFiles.txt
find -L $FC_DIR -name "*.FIT" -type f | sort > $FC_FIT_FILES

INFO_DIR=/project/nearsdc/data-apl/VESTA/FC/infofiles_new
rm -rf $INFO_DIR
mkdir -p $INFO_DIR
./create_info_files kernels.txt $FC_FIT_FILES $INFO_DIR
