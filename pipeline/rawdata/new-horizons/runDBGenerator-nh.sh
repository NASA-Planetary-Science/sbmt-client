#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:   Josh Steele, adapted for New Horizons by James Peachey
# Redmine:     sbmt1dev issue #1304
# Description: Script that runs the DB Generator for a given instrument name and 
#               dbTableName
#-------------------------------------------------------------------------------

# Usage
if [ "$#" -lt 6 ]
then
  echo "runDBGenerator-nh.sh <bodyName> <instrumentName> <databaseTableName> <instrumentIndex> <pointingType (GASKELL or SPICE)> shared"	
  echo "runDBGenerator-nh.sh <bodyName> <instrumentName> <databaseTableName> <instrumentIndex> <pointingType (GASKELL or SPICE)> <model-name> <processing-version>"	
  echo "Example: runDBGenerator-nh.sh polycam CHARON_NIMMO2017_LORRI 0 GASKELL shared"
  exit 1
fi

# Command line parameters
bodyName=$1
instrumentName=$2
dbTableBaseName=$3
cameraIndex=$4
pointingType=$5
processingModelName=$6

pipelineTop="/project/sbmtpipeline"

processingVersion="latest"

if [ $processingModelName != "shared" ]
then
  processingVersion=$7
fi

echo "Processing Model Name: " $processingModelName
echo "Processing Version: " $processingVersion

rawdataTop="$pipelineTop/rawdata"
processedTop="$pipelineTop/processed"
deployedTop="/project/sbmt2/sbmt/data/bodies"
testServerTop="/project/sbmt2/sbmt/data/servers/multi-mission/test/$bodyName"

scriptDir="/project/sbmt2/sbmt/scripts"
importCmd="$scriptDir/import.sh"

srcTop="$processedTop/$bodyName"
destTop="$deployedTop/$bodyName"

dbRootUrl="file:///disks/d0180/htdocs-sbmt/internal/multi-mission/test"

releaseDir="$rawdataTop/$bodyName/$processingVersion"

logDir="$rawdataTop/$bodyName/$processingVersion/logs"
log="$logDir/processed2deployed.log"

echo "Starting runDBGenerator-nh.sh script (log file: $log)"

echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "Begin `date`" >> $log 2>&1


if test -d $srcTop/$processingVersion/$processingModelName/$instrumentName; then
  if test "x$dbTableBaseName" != x; then
    echo "nice $releaseDir/sbmt/bin/DatabaseGeneratorSql.sh --root-url $dbRootUrl --cameraIndex $cameraIndex $pointingType $dbTableBaseName > $logDir/DatabaseGeneratorSql.log 2>&1" >> $log 2>&1
    nice $releaseDir/sbmt/bin/DatabaseGeneratorSql.sh --root-url $dbRootUrl --cameraIndex $cameraIndex $pointingType $dbTableBaseName > $logDir/DatabaseGeneratorSql.log 2>&1
    if test $? -ne 0; then
      echo "Error while updating database." >> $log 2>&1
      exit 1
    fi
  else
    echo "Found directory for $instrumentName data, but no database table root name supplied on the command line. Skipping database generation." >> $log 2>&1
  fi
else
  echo "Did not find directory for $instrumentName data. Skipping database generation." >> $log 2>&1
fi

echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
echo "Finished runDBGenerator-nh.sh script"
