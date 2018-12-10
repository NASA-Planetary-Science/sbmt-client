#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:   Russell Turner and Alex Welsh, updated by Josh Steele for Bennu
# Description: Test version of script for transforming processed BENNU data into
#              deployed data format, shared data only, NO MODELS
#-------------------------------------------------------------------------------

# Usage
if [ "$#" -lt 1 ]
then
  echo "Shared data usage: processed2deployed-bennu.sh shared"
  exit 1
fi

# Command line parameters
processingModelName="shared"
processingVersion="shared"

bodyName="bennu"

pipelineTop="/sbmt/pipeline"

echo "Processing Version: " $processingVersion

rawdataTop="$pipelineTop/rawdata"
processedTop="$pipelineTop/processed"
#deployedTop="/project/sbmt2/sbmt/data/bodies"
deployedTop="/sbmt/sbmtdata"
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
. $(dirname "$0")/commonFuncs.sh
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# MAIN SCRIPT STARTS HERE.
#-------------------------------------------------------------------------------

#if test `whoami` != sbmt; then
#  echo "Run this script while logged into the sbmt account." >&2
#  exit 1
#fi

makeLogDir

echo "Starting processed2deployed-bennu.sh script (log file: $log)"
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "Begin `date`" >> $log 2>&1

createDirIfNecessary $destTop/shared

echo Rsyncing $srcTop/latest/shared to $destTop/shared... >> $log 2>&1
doRsyncDir $srcTop/latest/shared $destTop/shared

# fix any bad permissions
#$scriptDir/sbmt2-data-permissions.pl $destTop/shared

echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
echo "Finished processed2deployed-bennu.sh script"
