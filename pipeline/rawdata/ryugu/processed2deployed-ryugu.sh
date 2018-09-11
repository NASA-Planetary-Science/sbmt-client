#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:   Russell Turner and Alex Welsh
# Redmine:     sbmt1dev issue #1304
# Description: Test version of script for transforming processed JAXA data into
#              deployed data format
#-------------------------------------------------------------------------------

# Usage
if [ "$#" -lt 1 ]
then
  echo "Model data usage:  processed2deployed-ryugu.sh <model-name> <processing-version>"
  echo "Shared data usage: processed2deployed-ryugu.sh shared"
  exit 1
fi

# Command line parameters
processingModelName=$1
processingVersion=$2

bodyName="ryugu"

pipelineTop="/project/sbmtpipeline"

if [ $processingModelName = "shared" ]
then
  processingVersion="latest"
fi

echo "Processing Model Name: " $processingModelName
echo "Processing Version: " $processingVersion

rawdataTop="$pipelineTop/rawdata"
processedTop="$pipelineTop/processed"
deployedTop="/project/sbmt2/sbmt/data/bodies"
testServerTop="/project/sbmt2/sbmt/data/servers/multi-mission/test/$bodyName"

scriptDir="/project/sbmt2/sbmt/scripts"
importCmd="$scriptDir/import.sh"
#rsyncCmd='rsync -rlptgDH --copy-links'
rsyncCmd='rsync -rltgDH --copy-links'

srcTop="$processedTop/$bodyName"
destTop="$deployedTop/$bodyName"

logDir="$rawdataTop/$bodyName/$processingVersion/logs"
log="$logDir/processed2deployed.log"

#-------------------------------------------------------------------------------

# Create a directory if it doesn't already exist.
createDirIfNecessary() {
  dir="$1"
  if test "x$dir" = x; then
    echo "createDirIfNecessary: missing/blank directory argument." >> $log 2>&1
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
}

# Perform an rsync from the source to the destination.
doRsync() {
  src=$1
  dest=$2

  # Make sure source directories end in a slash.
  if test -d $src; then
    src=`echo $src | sed 's:/*$:/:'`
  fi

  echo nice time $rsyncCmd $src $dest >> $log 2>&1
  nice time $rsyncCmd $src $dest >> $log 2>&1
  if test $? -ne 0; then
    echo "Failed to rsync $src $dest" >> $log 2>&1
    exit 1
  fi

  echo "" >> $log 2>&1
}

# Perform an rsync from the source to the destination. Both must be directories.
doRsyncDir() {
  src=$1
  dest=$2
  if test ! -e $src; then
    echo "Source $src does not exist" >> $log 2>&1
    exit 1
  fi
  if test ! -d $src; then
    echo "Source $src is unexpectedly not a directory." >> $log 2>&1
    exit 1
  fi
  if test -e $dest -a ! -d $dest; then
    echo "Destination $dest exists but is unexpectedly not a directory." >> $log 2>&1
    exit 1
  fi
  createDirIfNecessary $dest
  doRsync $src $dest
}

# Perform an rsync from a source directory to the destination, but only if the
# source directory exists.
doRsyncDirIfNecessary() {
  src=$1
  dest=$2
  if test -e $src; then
    doRsyncDir $src $dest
  fi
}

makeLogDir() {
  if test -e $logDir -a ! -d $logDir; then
    echo "Log directory $logDir exists but is not a directory." >&2
    exit 1
  fi
  mkdir -p $logDir
  if test $? -ne 0; then
    echo "Cannot create log directory $logDir." >&2
    exit 1
  fi
}

#-------------------------------------------------------------------------------
# MAIN SCRIPT STARTS HERE.
#-------------------------------------------------------------------------------

if test `whoami` != sbmt; then
  echo "Run this script while logged into the sbmt account." >&2
  exit 1
fi

echo "Starting processed2deployed-ryugu.sh script (log file: $log)"

echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "Begin `date`" >> $log 2>&1

if [ $processingModelName = "shared" ]
then

  createDirIfNecessary $destTop/shared

  echo Rsyncing $srcTop/latest/shared to $destTop/shared... >> $log 2>&1
  doRsyncDir $srcTop/latest/shared $destTop/shared

  # fix any bad permissions
  $scriptDir/sbmt2-data-permissions.pl $destTop/shared
else

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
  
  # if there are sum files make the following links, else (no images delivered) make links to onc and tir *** CANNOT HANDLE TIR SUMFILES, ONYL WORKS WITH ONC SUMFILES ***
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
echo "Finished processed2deployed-ryugu.sh script"

