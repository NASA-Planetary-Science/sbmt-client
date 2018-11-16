#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:   Russell Turner and Alex Welsh, updated by Josh Steele for Bennu
# Description: Test version of script for transforming processed BENNU data into
#              deployed data format, Model ONLY, no shared data
#-------------------------------------------------------------------------------

# Usage
if [ "$#" -lt 2 ]
then
  echo "Model data usage:  modelProcessed2deployed-bennu.sh <model-name> <processing-version>"
  exit 1
fi

# Command line parameters
processingModelName=$1
processingVersion=$2

bodyName="bennu"

echo "Processing Model Name: " $processingModelName
echo "Processing Version: " $processingVersion

pipelineTop="/project/sbmtpipeline"

rawdataTop="$pipelineTop/rawdata"
processedTop="$pipelineTop/processed"

scriptDir="/project/sbmt2/sbmt/scripts"

deployedTop="/project/sbmt2/sbmt/data/bodies"
testServerTop="/project/sbmt2/sbmt/data/servers/multi-mission/test/$bodyName"

scriptDir="/sbmt/scripts"
importCmd="$scriptDir/import.sh"
rsyncCmd='rsync -rlptgDH --copy-links'

srcTop="$processedTop/$bodyName"
destTop="$deployedTop/$bodyName"

logDir="$rawdataTop/$bodyName/$processingVersion/logs"
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

echo "Starting processed2deployed-bennu.sh script (log file: $log)"
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "Begin `date`" >> $log 2>&1

echo Rsyncing $srcTop/$processingVersion/$processingModelName to $destTop/$processingModelName-$processingVersion... >> $log 2>&1

doRsyncDir $srcTop/$processingVersion/$processingModelName $destTop/$processingModelName-$processingVersion

# Correct permissions.
echo Correcting permissions >> $log 2>&1
$scriptDir/sbmt2-data-permissions.pl $destTop/$processingModelName-$processingVersion

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

ln -s $destTop/$processingModelName-$processingVersion $testServerTop/$processingModelName
if test $? -ne -0; then
  echo "Unable to create the symbolic link $testServerTop/$processingModelName" >> $log 2>&1
  exit 1
fi

# if there are sum files make the following links, else (no images delivered) make links to
if [ -d "$srcTop/$processingVersion/$processingModelName/mapcam" ]; then
  # James to Josh: these should be checked for success. Probably need a new function for
  # doMakeLink or some such.
  # set the soft link to the shared mapcam directory
  ln -s ../../shared/mapcam/images $destTop/$processingModelName-$processingVersion/mapcam/images
  ln -s ../../shared/mapcam/gallery $destTop/$processingModelName-$processingVersion/mapcam/gallery
  ln -s ../../shared/mapcam/imagelist-info.txt $destTop/$processingModelName-$processingVersion/mapcam/imagelist-info.txt
  ln -s ../../shared/mapcam/imagelist-fullpath-info.txt $destTop/$processingModelName-$processingVersion/mapcam/imagelist-fullpath-info.txt
  ln -s ../../shared/mapcam/infofiles $destTop/$processingModelName-$processingVersion/mapcam/infofiles
  ln -s ../shared/"history" $destTop/$processingModelName-$processingVersion/"history"
  ln -s ../shared/lidar/ $destTop/$processingModelName-$processingVersion/lidar
else
  ln -s ../shared/mapcam $destTop/$processingModelName-$processingVersion/mapcam
  ln -s ../shared/"history" $destTop/$processingModelName-$processingVersion/"history"
  ln -s ../shared/lidar/ $destTop/$processingModelName-$processingVersion/lidar
fi

# if there are sum files make the following links, else (no images delivered) make links to
if [ -d "$srcTop/$processingVersion/$processingModelName/polycam" ]; then
  # James to Josh: these should be checked for success. Probably need a new function for
  # doMakeLink or some such.
  # set the soft link to the shared polycam directory
  ln -s ../../shared/polycam/images $destTop/$processingModelName-$processingVersion/polycam/images
  ln -s ../../shared/polycam/gallery $destTop/$processingModelName-$processingVersion/polycam/gallery
  ln -s ../../shared/polycam/imagelist-info.txt $destTop/$processingModelName-$processingVersion/polycam/imagelist-info.txt
  ln -s ../../shared/polycam/imagelist-fullpath-info.txt $destTop/$processingModelName-$processingVersion/polycam/imagelist-fullpath-info.txt
  ln -s ../../shared/polycam/infofiles $destTop/$processingModelName-$processingVersion/polycam/infofiles
  ln -s ../shared/"history" $destTop/$processingModelName-$processingVersion/"history"
  ln -s ../shared/lidar/ $destTop/$processingModelName-$processingVersion/lidar
else
  ln -s ../shared/polycam $destTop/$processingModelName-$processingVersion/polycam
  ln -s ../shared/"history" $destTop/$processingModelName-$processingVersion/"history"
  ln -s ../shared/lidar/ $destTop/$processingModelName-$processingVersion/lidar
fi

echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
echo "Finished processed2deployed-bennu.sh script"

