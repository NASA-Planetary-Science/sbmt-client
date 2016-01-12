#!/bin/bash

# Script for generating CERES infofiles for SBMT. Generates
# the SPICE makefile and executes the create_info_files program.
#
# Before executing this script, run compile.sh and mkmetakernel.pl.
#
# Ceres images are downloaded by Dave Bazell via cron job to the 
# DAWN project dir at /project/dawn/data2 and copied to the pipeline
# directory /project/sbmtpipeline/rawdata/dawn/ceres using script
# get_ceres_data.sh.
#
# Final step is to copy the new images into 
# /project/nearsdc/data/GASKELL/CERES/FC/images, 
# copy the new infofiles to
# /project/nearsdc/data/GASKELL/CERES/FC/infofiles, 
# and merge the new imagelist.txt and uniqFcFiles.txt with the 
# existing (cat f1.txt f2.txt > merged.txt.
#

cd `dirname $0`

# Target body
BODYNAME=CERES
echo "Generating $BODYNAME infofiles"

DATA_DIR=/project/sbmtpipeline/rawdata/dawn/ceres
FC_DIR=$DATA_DIR/fc
FIT_FILE_LIST=$DATA_DIR/allFcFiles.txt
OUTPUT_DIR=/project/sbmtpipeline/processed/dawn/ceres

# Create the metakernel.
#/usr/local/bin/perl $OUTPUT_DIR/mkmetakernel.pl

# Create a list of all .FIT files to input to the infofile generator.
# It outputs an imagelist.txt which can be used for fixed-list queries.
IMAGE_LIST=$OUTPUT_DIR/imagelist.txt
find -L $FC_DIR -name "*.FIT" -type f | sort > $FIT_FILE_LIST
echo "Finished writing $FIT_FILE_LIST"

# Make a copy with path prefix removed, to be used by database generator
awk -F: -v var=${FC_DIR} '{gsub(var,"/GASKELL/CERES/FC/images");print}' $FIT_FILE_LIST >$OUTPUT_DIR/uniqFcFiles.txt

INFO_DIR=$OUTPUT_DIR/infofiles
rm -rf $INFO_DIR
mkdir -p $INFO_DIR

# Generate the infofiles
./create_info_files_ceres $OUTPUT_DIR/kernels_ceres.mk $FIT_FILE_LIST $INFO_DIR $BODYNAME $IMAGE_LIST
chmod -R g+w $INFO_DIR/*