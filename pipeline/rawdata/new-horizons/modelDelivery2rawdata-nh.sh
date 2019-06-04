#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:      Russell Turner and Alex Welsh, updated by Josh Steele for Bennu
#                 and for New Horizons by James Peachey
# Description:    Test version of script for transforming delivered data into
#                 rawdata format, model data ONLY, no shared data
#-------------------------------------------------------------------------------

missionShortName="nh"

pipelineTop="/project/sbmtpipeline"
deliveriesTop="/project/sbmtpipeline/deliveries-$missionShortName"

# Usage
if [ "$#" -lt 6 ]
then
  echo "Model data usage:  modelDelivered2rawdata-$missionShortName.sh <delivered-body-name> <delivered-model-name> <delivered-version> <processed-body-name> <processed-model-name> <processing-version>"
  exit 1
fi

# Command line parameters
deliveredBodyName=$1
deliveredModelName=$2
deliveredVersion=$3
processingBodyName=$4
processingModelName=$5
processingVersion=$6

echo "Delivered version: " $deliveredVersion
echo "Delivered model name: " $deliveredModelName
echo "Processing version: " $processingVersion
echo "Processing model name: " $processingModelName

rawTop="$pipelineTop/rawdata"
processedTop="$pipelineTop/processed"

scriptDir="/project/sbmt2/sbmt/scripts"
importCmd="$scriptDir/import.sh"
rsyncCmd='rsync -rlptgDH --copy-links'

srcTop="$deliveriesTop/$deliveredBodyName/$deliveredVersion"
destTop="$rawTop/$processingBodyName/$processingVersion"

releaseDir="$destTop"

logDir="$destTop/logs"
log="$logDir/delivery2rawdata.log"

#-------------------------------------------------------------------------------
# Reference to commonFuncs file, which contains funcs common to pipeline scripts
. $(dirname "$0")/commonFuncs.sh
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# MAIN SCRIPT STARTS HERE.
#-------------------------------------------------------------------------------

if test `whoami` = sbmt; then
  echo "Run this script while logged in as yourself." >&2
  exit 1
fi

if [ $processingModelName = "shared" ]
then
   echo "Error: this script doesn't process 'shared' data" >&2
   exit 1
fi

export SBMTROOT="$releaseDir/sbmt"
export SAAVTKROOT="$releaseDir/saavtk"

echo "Starting deliveries2rawdata-$missionShortName.sh script (log file: $log)"

makeLogDir

echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "Begin `date`" >> $log 2>&1

# Import everything from the delivery directory

manifest="aamanifest.txt"
if test ! -f $srcTop/$deliveredModelName/$manifest; then
  echo "No manifest $srcTop/$deliveredModelName/$manifest" >> $log 2>&1
  exit 1
fi

if test ! -d $destTop/$processingModelName; then
  mkdir -p $destTop/$processingModelName
  if test $? -ne 0; then
    echo "Cannot create destination directory $destTop/$processingModelName" >> $log 2>&1
    exit 1
  fi
fi

# Confirm the source directory points somewhere.
if test ! -d $srcTop/$deliveredModelName; then
  echo "Error: directory $srcTop/$deliveredModelName does not exist" >&2
  exit 1
fi

# otherwise, assume this is model data and copy over into a new model
createDirIfNecessary $destTop/$processingModelName/shape

# copy the manifest as a backup
doRsync $srcTop/$deliveredModelName/aamanifest.txt $destTop/$processingModelName/aamanifest.txt

# Define a series of directories to copy
declare -a dirsToCopy=("coloring" "lorri/sumfiles" "lorri/infofiles" "leisa/sumfiles" "leisa/infofiles" "mvic/sumfiles" "mvic/infofiles")
# Define a series of specific files to copy
declare -a filesToCopy=("lorri/make_sumfiles.in" "lorri/imagelist-info.txt" "leisa/make_sumfiles.in" "leisa/imagelist-info.txt" "mvic/make_sumfiles.in" "mvic/imagelist-info.txt")

# copy the shape model
doRsyncDir $srcTop/$deliveredModelName/shape $destTop/$processingModelName/shape

for dir in ${dirsToCopy[@]}
do
  if [ -d "$srcTop/$deliveredModelName/$dir" ]
  then
    # copy the files
    doRsyncDir $srcTop/$deliveredModelName/$dir $destTop/$processingModelName/$dir
  fi
done

for file in ${filesToCopy[@]}
do
  if [ -f "$srcTop/$deliveredModelName/$file" ]
  then
    # copy the files
    doRsync $srcTop/$deliveredModelName/$file $destTop/$processingModelName/$file
  fi
done

echo fixing permissions
$scriptDir/data-permissions.pl $destTop/$processingModelName

echo removing unused files
#rm -rf $destTop/shared/*/images/*.tgz
#rm -rf $destTop/shared/*/images/*.d
#rm -rf $destTop/shared/*/images/index.html*

echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
echo "Finished modelDdelivery2rawdata-$missionShortName.sh script"
