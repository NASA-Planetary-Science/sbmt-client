#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:   Russell Turner and Alex Welsh, updated by Josh Steele for Bennu
#              and for New Horizons by James Peachey
# Description: Test version of script for transforming processed data into
#              deployed data format, model data ONLY, no shared data
#-------------------------------------------------------------------------------

missionShortName="nh"

# Usage
if [ "$#" -lt 3 ]
then
  echo "Model data usage:  modelProcessed2deployed-$missionShortName.sh <body-name> <model-name> <processing-version>"
  exit 1
fi

# Command line parameters
processingBodyName=$1
processingModelName=$2
processingVersion=$3

echo "Processing Model Name: " $processingModelName
echo "Processing Version: " $processingVersion

pipelineTop="/project/sbmtpipeline"

rawdataTop="$pipelineTop/rawdata"
processedTop="$pipelineTop/processed"

deployedTop="/project/sbmt2/sbmt/data/bodies"
testServerTop="/project/sbmt2/sbmt/data/servers/multi-mission/test/$processingBodyName"

scriptDir="/project/sbmt2/sbmt/scripts"
importCmd="$scriptDir/import.sh"
rsyncCmd='rsync -rlptgDH --copy-links'

srcTop="$processedTop/$processingBodyName"
destTop="$deployedTop/$processingBodyName"

logDir="$rawdataTop/$processingBodyName/$processingVersion/logs"
log="$logDir/processed2deployed.log"

#-------------------------------------------------------------------------------
# Reference to commonFuncs file, which contains funcs common to pipeline scripts
. $(dirname "$0")/commonFuncs.sh
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# MAIN SCRIPT STARTS HERE.
#-------------------------------------------------------------------------------

if test `whoami` != sbmt; then
  echo "Run this script while logged into the sbmt account." >&2
  exit 1
fi

echo "Starting processed2deployed-$missionShortName.sh script (log file: $log)"
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "Begin `date`" >> $log 2>&1

echo Rsyncing $srcTop/$processingVersion/$processingModelName to $destTop/$processingModelName-$processingVersion... >> $log 2>&1

doRsyncDir $srcTop/$processingVersion/$processingModelName $destTop/$processingModelName-$processingVersion

# Correct permissions.
echo Correcting permissions >> $log 2>&1
$scriptDir/sbmt2-data-permissions.pl $destTop/$processingModelName-$processingVersion

# THIS NEXT BLOCK NOT REALLY TESTED YET.
# Create additional symbolic links for each imager.
imager=lorri
if test -d "$destTop/shared/$imager"; then
  if test -d "$destTop/$processingModelName-$processingVersion/$imager"; then
  
    # Delivery came with some imager files, so just link files that did not come with the delivery.
    cd "$destTop/$processingModelName-$processingVersion/$imager"
    for sharedItem in ../../shared/$imager/*; do
      item=`echo $sharedItem | s:.*/::`
      if test ! -e $item; then
        createSymbolicLink $sharedItem $item
      fi
    done
    
  else
  
    # Delivery did not include imager. Link to shared imager data at the top level.
    createSymbolicLink ../shared/$imager $destTop/$processingModelName-$processingVersion/$imager
  fi
fi

# Update symbolic link to current model in the test area; this is used by the database generator.
if test -h $testServerTop/$processingModelName; then
  rm $testServerTop/$processingModelName
  if test $? -ne -0; then
    echo "Unable to remove the symbolic link $testServerTop/$processingModelName" >> $log 2>&1
    exit 1
  fi
elif test -e $testServerTop/$processingModelName; then
  echo "File or directory $testServerTop/$processingModelName exists, but is not a symbolic link" >> $log 2>&1
  exit 1
fi

if test ! -d $testServerTop; then
  mkdir $testServerTop
  if test $? -ne 0; then
    echo "Unable to create directory $testServerTop" >> $log 2>&1
    exit 1
  fi
fi

createSymbolicLink $destTop/$processingModelName-$processingVersion $testServerTop/$processingModelName
if test $? -ne -0; then
  exit 1
fi

echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
echo "Finished processed2deployed-$missionShortName.sh script"

