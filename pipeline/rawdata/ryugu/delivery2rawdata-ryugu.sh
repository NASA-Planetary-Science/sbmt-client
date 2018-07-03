#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:      Russell Turner and Alex Welsh
# Redmine:        sbmt1dev issue #1304
# Description:    Test version of script for transforming delivered JAXA data into
#                 rawdata format
#-------------------------------------------------------------------------------

# usage
if [ "$#" -eq 0 ]
then
  echo "Model data usage:  delivered2rawdata-ryugu.sh <delivered-model-name> <delivered-date> [ <processed-model-name> <processed-date> ]"
  echo "Shared data usage: delivered2rawdata-ryugu.sh shared"
  exit 1
fi

# command line parameters
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

  
# determine the deliveries directory
deliveriesTop="$pipelineTop/deliveries"

# models that start with "jaxa" are assumed to be from the -hyb2 delivery directory
fromHyb2=`expr "$processingModelName" : 'jaxa.*'`
if [ $fromHyb2 -gt 0 ]
then
  deliveriesTop=$deliveriesTop'-hyb2'
fi

# shared data is also assumed to be from the -hyb2 delivery directory
if [ $deliveredModelName = "shared" ]
then
  deliveriesTop=$deliveriesTop'-hyb2'
  processingVersion="latest"
  deliveredVersion="latest"
fi

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

srcTop="$deliveriesTop/$bodyName/$deliveredVersion"
destTop="$rawTop/$bodyName/$processingVersion"
log="$destTop/logs/delivery2rawdata-model.log"

# echo "Source Top: " $srcTop
# echo "Dest Top: " $destTop

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
#  echo "--------------------------------------------------------------------------------" >> $log 2>&1

  # Make sure source directories end in a slash.
  if test -d $src; then
    src=`echo $src | sed -e 's:/*$:/:'`
  fi

  echo nice time $rsyncCmd $src $dest >> $log 2>&1
  nice time $rsyncCmd $src $dest >> $log 2>&1
  if test $? -ne 0; then
    exit 1;
  fi
#  echo "--------------------------------------------------------------------------------" >> $log 2>&1
  echo "" >> $log 2>&1
)

# Perform an rsync from the source to the destination. Both must be directories.
# TODO add error checking.
doRsyncDir() (
  src=$1
  dest=$2
#  echo "Source: " $src
#  echo "Dest: " $dest
  createDirIfNecessary $dest
  doRsync $src $dest
)

#-------------------------------------------------------------------------------
# MAIN SCRIPT STARTS HERE.
#-------------------------------------------------------------------------------

echo "Starting deliveries2rawdata-ryugu.sh script (log file: $log)"
mkdir -p $destTop
mkdir -p $destTop/logs
# fix any bad permissions
$scriptDir/data-permissions.pl $destTop/logs

echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "Begin `date`" >> $log 2>&1

# Import everything from the delivery directory

if [ $processingModelName = "shared" ]
then
   # if the model name is "shared" then we only copy over the new shared data
   createDirIfNecessary $destTop/shared
   createDirIfNecessary $destTop/shared/onc

   for dateDir in `ls $srcTop/onc/FromONC_CoI_Server`
   do
     echo Rsyncing image files from ryugu/latest/onc/FromONC_CoI_Server/$dateDir... >> $log 2>&1
     doRsyncDir $srcTop/onc/FromONC_CoI_Server/$dateDir/ $destTop/shared/onc/$dateDir
   done
else
   # otherwise, assume this is model data and copy over into a new model
   createDirIfNecessary $destTop/$processingModelName/shape
   createDirIfNecessary $destTop/$processingModelName/coloring
   createDirIfNecessary $destTop/$processingModelName/onc

   # copy the manifest as a backup
   doRsync $srcTop/$deliveredModelName/aamanifest.txt $destTop/$processingModelName/aamanifest.txt

   # copy the shape model
   doRsyncDir $srcTop/$deliveredModelName/shape $destTop/$processingModelName/shape

   # copy the coloring files
   doRsyncDir $srcTop/$deliveredModelName/coloring $destTop/$processingModelName/coloring

   # copy the onc imaging files
   doRsyncDir $srcTop/$deliveredModelName/imaging $destTop/$processingModelName/onc
fi


# fix any bad permissions
$scriptDir/data-permissions.pl $destTop/$processingModelName


echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
echo "Finished deliveries2rawdata-ryugu.sh script"
