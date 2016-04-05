#!/bin/bash

# Script for generating CERES infofiles for SBMT. Generates
# the SPICE makefile and executes the create_info_files program.
#
# Ceres images are downloaded by Dave Bazell via cron job to the 
# DAWN project dir at /project/dawn/data2 and soft-linked to the 
# pipeline directory /project/sbmtpipeline/rawdata/dawn/ceres 
# using script get_ceres_data.sh.
#
# Final step is to copy (or rsync) the new images into 
# /project/nearsdc/data/GASKELL/CERES/FC/images, 
# copy the new infofiles to
# /project/nearsdc/data/GASKELL/CERES/FC/infofiles, 
# and merge uniqFcFiles.txt with the existing (or  
# overwrite if regenerating the database tables). 
# Then call DatabaseGeneratorSql. See commented code
# at bottom of script for specifics.
#

echo "I'm in run_ceres.sh!!"
exit 1

cd `dirname $0`

# Target body
BODYNAME=CERES
echo "Generating $BODYNAME infofiles"

DATA_DIR=/project/sbmtpipeline/rawdata/dawn/ceres
FC_DIR=$DATA_DIR/fc
FIT_FILE_LIST=$DATA_DIR/allFcFiles.txt
OUTPUT_DIR=/project/sbmtpipeline/processed/dawn/ceres

# Create the metakernel.
/usr/local/bin/perl $OUTPUT_DIR/mkmetakernel.pl

# Create a list of all .FIT files to input to the infofile generator.
# It outputs an imagelist.txt which can be used for fixed-list queries.
IMAGE_LIST=$OUTPUT_DIR/imagelist.txt
find -L $FC_DIR -name "*.FIT" -type f | sort > $FIT_FILE_LIST
echo "Finished writing $FIT_FILE_LIST"

# Filter the images
python filterGoodFcImages.py $FIT_FILE_LIST >$OUTPUT_DIR/uniqFcFilesFullPath.txt

# Make a copy of the image list with path prefix removed, to be used by database generator
awk -F: -v var=${FC_DIR} '{gsub(var,"/GASKELL/CERES/FC/images");print}' $OUTPUT_DIR/uniqFcFilesFullPath.txt >$OUTPUT_DIR/uniqFcFiles.txt
echo "Finished writing $OUTPUT_DIR/uniqFcFiles.txt"

INFO_DIR=$OUTPUT_DIR/infofiles
rm -rf $INFO_DIR
mkdir -p $INFO_DIR

# Generate the infofiles
COMMAND="./create_info_files_ceres $OUTPUT_DIR/kernels_ceres.mk $FIT_FILE_LIST $INFO_DIR $BODYNAME $IMAGE_LIST"
echo -e "$COMMAND \n"
$COMMAND

# Copy the good images to the live directory: 
#for FC_IMAGE_FILE in $(cat $OUTPUT_DIR/uniqFcFilesFullPath.txt) ; do
#  cp $FC_IMAGE_FILE /project/nearsdc/data/GASKELL/CERES/FC/images
#done;

# Copy the infofiles to the live directory:
#rsync -a --delete /project/sbmtpipeline/processed/dawn/ceres/infofiles/ /project/nearsdc/data/GASKELL/CERES/FC/infofiles/

# Concatenate the uniqFcFiles if appending to the database (what about duplicates?):
#cat /project/sbmtpipeline/processed/dawn/ceres/uniqFcFiles.txt /project/nearsdc/data/GASKELL/CERES/FC/uniqFcFiles.txt > /project/nearsdc/data/GASKELL/CERES/FC/uniqFcFiles_all.txt
#mv /project/nearsdc/data/GASKELL/CERES/FC/uniqFcFiles_all.txt /project/nearsdc/data/GASKELL/CERES/FC/uniqFcFiles.txt 

# If not appending, just move over the new uniqFcFiles
#cp /project/sbmtpipeline/processed/dawn/ceres/uniqFcFiles.txt /project/nearsdc/data/GASKELL/CERES/FC/

# Now run DatabaseGeneratorSql. Option --append-tables to append. This writes to _beta tables. For cron job, append to main tables.
