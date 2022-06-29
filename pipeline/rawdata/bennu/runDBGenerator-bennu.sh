#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:   Josh Steele
# Redmine:     sbmt1dev issue #1304
# Description: Script that runs the DB Generator for a given instrument,
#              model, processing/delivery version and pointing type.
#-------------------------------------------------------------------------------

# Usage
if [ "$#" -lt 5 ]
then
  echo "runDBGenerator-bennu.sh <instrumentName> <modelName> <modelDirectoryName> <processingVersion> <pointingType (GASKELL or SPICE)>"	
  echo "Example: runDBGenerator-bennu.sh polycam ALTWG-SPC-v20190828 altwg-spc-v20190828 redmine-1899 GASKELL"
  exit 1
fi

# Command line parameters
instrumentName=$1
processingModelName=$2
processingModelDirectory=$3
processingVersion=$4
pointingType=$5

pipelineTop="/project/sbmtpipeline"

echo "Processing Model Name: " $processingModelDirectory
echo "Processing Version: " $processingVersion

bodyName="bennu"
bodyId="RQ36"
rawdataTop="$pipelineTop/rawdata"
processedTop="$pipelineTop/processed"
deployedTop="/project/sbmt2/sbmt/data/bodies"
testServerTop="/project/sbmt2/sbmt/data/servers/multi-mission/test/$bodyName"

scriptDir="/project/sbmt2/sbmt/scripts"
importCmd="$scriptDir/import.sh"
swCheckoutTop="$rawdataTop/$bodyName/$processingVersion"

srcTop="$processedTop/$bodyName"
destTop="$deployedTop/$bodyName"

dbRootUrl="file:///disks/d0180/htdocs-sbmt/internal/multi-mission/test"

logDir="$rawdataTop/$bodyName/$processingVersion/logs"
log="$logDir/processed2deployed.log"

echo "Starting runDBGenerator-bennu.sh script (log file: $log)"

echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "Begin `date`" >> $log 2>&1

if test -d $destTop/$processingModelDirectory/$instrumentName; then
  echo "nice $swCheckoutTop/sbmt/bin/DatabaseGeneratorSql.sh --root-url $dbRootUrl --body $bodyId --author $processingModelName --instrument $instrumentName $pointingType $dbTableBaseName > $logDir/DatabaseGeneratorSql.log 2>&1" >> $log 2>&1
  nice $swCheckoutTop/sbmt/bin/DatabaseGeneratorSql.sh --root-url $dbRootUrl --body $bodyId --author $processingModelName --instrument $instrumentName $pointingType $dbTableBaseName > $logDir/DatabaseGeneratorSql.log 2>&1
  if test $? -ne 0; then
    echo "Error while updating database." >> $log 2>&1
    exit 1
  fi
else
  echo "Did not find directory for $processingModelName $instrumentName data. Skipping database generation." >> $log 2>&1
fi

echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
echo "Finished runDBGenerator-bennu.sh script"
