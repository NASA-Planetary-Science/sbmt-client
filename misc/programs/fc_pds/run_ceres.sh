#!/bin/bash

# Script for running the create_info_files program
cd `dirname $0`

# Target body
BODYNAME=CERES

# Create a list of all fit files
FC_DIR=/project/nearsdc/data/GASKELL/CERES/FC/images
FC_FIT_FILES=/project/nearsdc/data/GASKELL/CERES/allFcFiles.txt

# Create image list, for fixed list query
IMAGE_LIST=/project/nearsdc/data/GASKELL/CERES/FC/imagelist.txt
find -L $FC_DIR -name "*.FIT" -type f | sort > $FC_FIT_FILES

INFO_DIR=/project/nearsdc/data/GASKELL/CERES/FC/infofiles
rm -rf $INFO_DIR
mkdir -p $INFO_DIR
./create_info_files kernels_ceres.txt $FC_FIT_FILES $INFO_DIR $BODYNAME $IMAGE_LIST
chmod -R g+w $INFO_DIR/*