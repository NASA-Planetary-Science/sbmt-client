#!/bin/bash

#
# This script batch-processes OLA L2 (level-2) lidar data files 
# into a "shot database" (set of text files) injestible by SBMT.
#
# The intended use is to run this script first. Once it completes,
# it will have generated a directory structure containing one folder
# for each OLA L2 file, with folder name the L2 file's basename.
# The folder will contain one text file per voxel, or "cube", containing
# the timestamp, spacecraft position, and intercept vector in body-fixed
# coordinates of each OLA shot in that cube.
#
# Once this script has completed, a merge script must be called to merge 
# the contents of the same-named "cube" files from each L2 basename folder. 
# This final merged set of "cube" files is then copied into the live 
# directory /GASKELL/RQ36_V3/OLA/cubes.
#
#########################################################################
# This script has not yet been used operationally.
#########################################################################
#

SBMT_DATA_DIR_SUFFIX=/GASKELL/RQ36_V3/OLA
INPUT_DIR=/project/sbmtpipeline/rawdata/osirisrex/OLA
OUTPUT_DIR=/project/sbmtpipeline/processed/osirisrex/OLA


# Create a list of all level 2 files for SBMT
find -L $INPUT_DIR -maxdepth 1 -name "*.l2" -type f | sort > $OUTPUT_DIR/allOlaFiles.txt

# Change the path prefix for each entry in the list of l2 files to be relative to the SBMT data path
touch $OUTPUT_DIR/allOlaFiles2.txt
awk -v old=${INPUT_DIR} -v new=${SBMT_DATA_DIR_SUFFIX} '{gsub(old,new);print>"allOlaFiles2.txt"}' allOlaFiles.txt
mv $OUTPUT_DIR/allOlaFiles2.txt $OUTPUT_DIR/allOlaFiles.txt

# OpenGrid (location of qsub, with paths for gotham and south park clusters)
for version in ge-GE2011.11-11p1 ge-GE2011-11p1 sge61; do
    if [ -e /opt/${version}/default/common/settings.sh ]; then
        export LD_LIBRARY_PATH=/opt/${version}/lib/linux-x64:${LD_LIBRARY_PATH}
        source /opt/${version}/default/common/settings.sh
        break
    fi
done

# Submit each L2 file for batch processing
for file in $SOURCE_INPUT_DIR/*.l2 ; do
	# Pass just the basename of the OLA L2 file to the script, no path or extension. OlaCubesGenerator will create the folder.
	L2FOLDER=$(basename $file)
    COMMAND = "qsub -cwd -S /bin/bash -o $file.out -e $file.err ./generate_ola_cubes.sh $L2FOLDER"
    echo -e "$COMMAND \n"
    $COMMAND
done

