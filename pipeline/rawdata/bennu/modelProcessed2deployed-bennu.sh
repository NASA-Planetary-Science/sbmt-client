#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:   Russell Turner and Alex Welsh, updated by Josh Steele for Bennu
# Description: Test version of script for transforming processed BENNU data into
#              deployed data format, Model ONLY, no shared data
#-------------------------------------------------------------------------------

# Usage
if [ "$#" -lt 1 ]
then
  echo "Model data usage:  processed2deployed-bennu.sh <model-name> <processing-version>"
  exit 1
fi

# Command line parameters
processingModelName=$1
processingVersion=$2

bodyName="bennu"

pipelineTop="/sbmt/pipeline"

echo "Processing Model Name: " $processingModelName
echo "Processing Version: " $processingVersion

rawdataTop="$pipelineTop/rawdata"
processedTop="$pipelineTop/processed"
#deployedTop="/project/sbmt2/sbmt/data/bodies"
deployedTop="/sbmt/sbmtdata/"
#testServerTop="/project/sbmt2/sbmt/data/servers/multi-mission/test/$bodyName"
testServerTop="/sbmt/data/servers/multi-mission/test/$bodyName"

scriptDir="/sbmt/scripts"
importCmd="$scriptDir/import.sh"
#rsyncCmd='rsync -rlptgDH --copy-links'
rsyncCmd='rsync -rltgDH --copy-links'

srcTop="$processedTop/$bodyName"
destTop="$deployedTop/$bodyName"

logDir="$rawdataTop/$bodyName/$processingVersion/logs"
log="$logDir/processed2deployed.log"

#-------------------------------------------------------------------------------
# Reference the common funcs file
. commonFuncs.sh
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
  
###################### FIX THIS FOR BENNU ##################################
# if there are sum files make the following links, else (no images delivered) make links to 
if [ -d "$srcTop/$processingVersion/$processingModelName/onc" ]
  then 
    # James to Josh: these should be checked for success. Probably need a new function for
    # doMakeLink or some such.
    # set the soft link to the shared onc directory
    ln -s ../../shared/onc/images $destTop/$processingModelName-$processingVersion/onc/images
    ln -s ../../shared/onc/gallery $destTop/$processingModelName-$processingVersion/onc/gallery
    ln -s ../../shared/onc/imagelist-info.txt $destTop/$processingModelName-$processingVersion/onc/imagelist-info.txt
    ln -s ../../shared/onc/imagelist-fullpath-info.txt $destTop/$processingModelName-$processingVersion/onc/imagelist-fullpath-info.txt
    ln -s ../../shared/onc/infofiles $destTop/$processingModelName-$processingVersion/onc/infofiles
    ln -s ../shared/"history" $destTop/$processingModelName-$processingVersion/"history"
    ln -s ../shared/tir/ $destTop/$processingModelName-$processingVersion/tir
    ln -s ../shared/lidar/ $destTop/$processingModelName-$processingVersion/lidar
  else
    ln -s ../shared/onc $destTop/$processingModelName-$processingVersion/onc
    ln -s ../shared/"history" $destTop/$processingModelName-$processingVersion/"history"
    ln -s ../shared/tir/ $destTop/$processingModelName-$processingVersion/tir
    ln -s ../shared/lidar/ $destTop/$processingModelName-$processingVersion/lidar
  fi
fi

echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
echo "Finished processed2deployed-bennu.sh script"

