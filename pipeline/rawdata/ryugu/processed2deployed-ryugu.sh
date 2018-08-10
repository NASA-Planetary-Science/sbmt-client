#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:   Russell Turner and Alex Welsh
# Redmine:     sbmt1dev issue #1304
# Description: Test version of script for transforming processed JAXA data into
#              deployed data format
#-------------------------------------------------------------------------------

# usage
if [ "$#" -eq 0 ]
then
  echo "Model data usage:  processed2deployed-ryugu.sh <model-name> <processed-date> [ <processed-model-name> <processed-date> ]"
  echo "Shared data usage: processed2deployed-ryugu.sh shared"
  exit 1
fi

pipelineTop="/project/sbmtpipeline"
bodyName="ryugu"

processingModelName="jaxa-001"
if [ "$#" -gt 0 ]
then
  processingModelName=$1
fi

processingVersion="20180628"
if [ "$#" -gt 1 ]
then
  processingVersion=$2
fi

if [ $processingModelName = "shared" ]
then
  processingVersion="latest"
fi

echo "Processing Model Name: " $processingModelName
echo "Processing Version: " $processingVersion

rawTop="$pipelineTop/rawdata"
processedTop="$pipelineTop/processed"
deployedTop="/project/sbmt2/sbmt/data/bodies"

scriptDir="/project/sbmt2/sbmt/scripts"
importCmd="$scriptDir/import.sh"
rsyncCmd='rsync -rlptgDH --copy-links'

log="$rawTop/$bodyName/$processingVersion/logs/processed2deployed-ryugu.log"

srcTop="$processedTop/$bodyName"
destTop="$deployedTop/$bodyName"

echo "srcTop: $srcTop"
echo "destTop: $destTop"
echo "log: $log"

#-------------------------------------------------------------------------------

# Create a directory if it doesn't already exist.
createDirIfNecessary() (
  dir="$1"
  if test "x$dir" = x; then
    echo "createDirIfNecessary: missing/blank directory." >> $log 2>&1
    exit 1
  fi

  if test ! -d $dir; then
    echo mkdir -p $dir >> $log 2>&1
    mkdir -p $dir >> $log 2>&1
    if test $? -ne 0; then
      echo "createDirIfNecessary: unable to create directory $dir." >> $log 2>&1
      exit 1
    fi
  fi
)

# Perform an rsync from the source to the destination.
doRsync() (
  src=$1
  dest=$2
  echo "--------------------------------------------------------------------------------" >> $log 2>&1

  # Make sure source directories end in a slash.
  if test -d $src; then
    src=`echo $src | sed -e 's:/*$:/:'`
  fi

  echo nice time $rsyncCmd $src $dest >> $log 2>&1
  nice time $rsyncCmd $src $dest >> $log 2>&1
  if test $? -ne 0; then
    exit 1;
  fi
  echo "--------------------------------------------------------------------------------" >> $log 2>&1
)

# Perform an rsync from the source to the destination. Both must be directories.
# TODO add error checking.
doRsyncDir() (
  src=$1
  dest=$2
  createDirIfNecessary $dest
  doRsync $src $dest
)

#-------------------------------------------------------------------------------
# MAIN SCRIPT STARTS HERE.
#-------------------------------------------------------------------------------

echo "Starting processed2deployed-ryugu.sh script (log file: $log)"

echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "Begin `date`" >> $log 2>&1

if [ $processingModelName = "shared" ]
then

  createDirIfNecessary $destTop/shared

  echo Rsyncing $srcTop/latest/shared to $destTop/shared... >> $log 2>&1
  doRsyncDir $srcTop/latest/shared $destTop/shared

  # fix any bad permissions
#  $scriptDir/data-permissions.pl $destTop/shared
else

  echo Rsyncing $srcTop/$processingVersion/$processingModelName to $destTop/$processingModelName-$processingVersion... >> $log 2>&1

  doRsyncDir $srcTop/$processingVersion/$processingModelName $destTop/$processingModelName-$processingVersion
  
  # if there are sum files make the following links, else (no images delivered) make links to onc and tir *** CANNOT HANDLE TIR SUMFILES, ONYL WORKS WITH ONC SUMFILES ***
  if [ -d "$srcTop/$processingVersion/$processingModelName/onc" ]
  then 
    # set the soft link to the shared onc directory
    ln -s ../../shared/onc/images $destTop/$processingModelName-$processingVersion/onc/images
    ln -s ../../shared/onc/gallery $destTop/$processingModelName-$processingVersion/onc/gallery
    ln -s ../../shared/onc/imagelist-info.txt $destTop/$processingModelName-$processingVersion/onc/imagelist-info.txt
    ln -s ../../shared/onc/imagelist-fullpath-info.txt $destTop/$processingModelName-$processingVersion/onc/imagelist-fullpath-info.txt
    ln -s ../../shared/onc/infofiles $destTop/$processingModelName-$processingVersion/onc/infofiles
    ln -s ../shared/"history" $destTop/$processingModelName-$processingVersion/"history"
    ln -s ../shared/tir/ $destTop/$processingModelName-$processingVersion/tir
  else
    ln -s ../shared/onc $destTop/$processingModelName-$processingVersion/onc
    ln -s ../shared/"history" $destTop/$processingModelName-$processingVersion/"history"
    ln -s ../shared/tir/ $destTop/$processingModelName-$processingVersion/tir
  fi

  # fix any bad permissions
  $scriptDir/data-permissions.pl $destTop/$processingModelName-$processingVersion

fi

echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
echo "Finished processed2deployed-ryugu.sh script"

