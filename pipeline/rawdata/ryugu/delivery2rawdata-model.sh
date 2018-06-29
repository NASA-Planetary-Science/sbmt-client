#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:      Russell Turner and Alex Welsh
# Redmine:        sbmt1dev issue #1304
# Description:    Test version of script for transforming delivered JAXA data into
#                 rawdata format
#-------------------------------------------------------------------------------
# Note: bodyName, deliveredVersion, deliveredModelName, processingVersion and processingModelName need to be configured for each model

pipelineTop="/project/sbmtpipeline"
bodyName="ryugu"

deliveredModelName="JAXA-001"
if [ "$#" -gt 0 ]
then
  deliveredModelName=$1
fi

deliveredVersion="20180628"
if [ $# -gt 1 ]
then
  deliveredVersion=$2
fi

processingModelName=`echo "$deliveredModelName" | sed 's/.*/\L&/'`
if [ "$#" -gt 2 ]
then
  processingModelName=$3
fi

processingVersion=$deliveredVersion
if [ "$#" -gt 3 ]
then
  processingVersion=$4
fi

deliveriesTop="$pipelineTop/deliveries"
fromHyb2=`expr "$processingModelName" : 'jaxa.*'`
echo $fromHyb2
if [ $fromHyb2 -gt 0 ]
then
  deliveriesTop=$deliveriesTop'-hyb2'
fi

echo "Deliveries Top: " $deliveriesTop
echo "Body name: " $bodyName
echo "Delivered version: " $deliveredVersion
echo "Delivered model name: " $deliveredModelName
echo "Processing version: " $processingVersion
echo "Processing model name: " $processingModelName

legacyTop="/project/sbmt2/sbmt/nearsdc/data"
rawTop="$pipelineTop/rawdata"
processedTop="$pipelineTop/processed"
deployedTop="/project/sbmt2/sbmt/data/bodies"

scriptDir="/project/sbmt2/sbmt/scripts"
importCmd="$scriptDir/import.sh"
rsyncCmd='rsync -rlptgDH --copy-links'
log="logs/delivery2rawdata-model.log"

srcTop="$deliveriesTop/$bodyName/$deliveredVersion"
destTop="$rawTop/$bodyName/$processingVersion"
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
  echo "Source: " $src
  echo "Dest: " $dest
  createDirIfNecessary $dest
  doRsync $src $dest
)

#-------------------------------------------------------------------------------
# MAIN SCRIPT STARTS HERE.
#-------------------------------------------------------------------------------
# echo "--------------------------------------------------------------------------------" >> $log 2>&1
# echo "Begin `date`" >> $log 2>&1

# Import everything from the 20180411 delivery.
# echo $srcTop/$deliveredModelName $destTop/$processingModelName
doRsyncDir $srcTop/$deliveredModelName $destTop/$processingModelName

# fix any bad permissions
$scriptDir/data-permissions.pl $destTop/$processingModelName

echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
