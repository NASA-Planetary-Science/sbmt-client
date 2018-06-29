#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:   Russell Turner and Alex Welsh
# Redmine:     sbmt1dev issue #1304
# Description: Test version of script for transforming processed JAXA data into
#              deployed data format
#-------------------------------------------------------------------------------
# Note: bodyName, deliveredVersion, deliveredModel, processingVersion and destModelName need to be configured for each model

bodyName="ryugu"
deliveredVersion="20180628"
deliveredModel="truth"
processingVersion="20180628"
destModelName="jaxa-001"

legacyTop="/project/sbmt2/sbmt/nearsdc/data"
pipelineTop="/project/sbmtpipeline"
processedTop="$pipelineTop/processed"
deployedTop="/project/sbmt2/sbmt/data/bodies"

scriptDir="/project/sbmt2/sbmt/scripts"
importCmd="$scriptDir/import.sh"
rsyncCmd='rsync -rlptgDH --copy-links'
log="logs/processed2deployed-shape.log"

srcTop="$processedTop/$bodyName"
destTop="$deployedTop/$bodyName"
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

echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "Begin `date`" >> $log 2>&1

echo "Source: " $srcTop/$processingVersion/$destModelName
echo "Dest: " $destTop/$destModelName-$processingVersion

doRsyncDir $srcTop/$processingVersion/$destModelName $destTop/$destModelName-$processingVersion

echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
