#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:   Josh Steele
# Redmine:     sbmt1dev issue #1304
# Description: Script that runs the DB Generator for a given instrument name and 
#               dbTableName
#-------------------------------------------------------------------------------

# Usage
if [ "$#" -lt 4 ]
then
  echo "runDBGenerator-bennu.sh <instrumentName> <databaseTableName> <instrumentIndex> <pointingType (GASKELL or SPICE)> shared"	
  echo "runDBGenerator-bennu.sh <instrumentName> <databaseTableName> <instrumentIndex> <pointingType (GASKELL or SPICE)> <model-name> <processing-version>"	
  echo "Example: runDBGenerator-bennu.sh polycam BENNU_ALTWG_SPC_V20181109B_POLYCAM_APL 0 GASKELL shared"
  exit 1
fi

# Command line parameters
instrumentName=$1
dbTableBaseName=$2
cameraIndex=$3
pointingType=$4
processingModelName=$5

pipelineTop="/project/sbmtpipeline"

processingVersion="latest"

if [ $processingModelName != "shared" ]
then
  processingVersion=$6
fi

echo "Processing Model Name: " $processingModelName
echo "Processing Version: " $processingVersion

bodyName="bennu"
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

echo "Starting runDBGenerator-bennu.sh script (log file: $log)"

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
echo "Finished runDBGenerator-bennu.sh script"
