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
  echo "Model data usage:  processed2deployed-ryugu.sh <model-name> <processed-date> [ aizu | stage | apl ]"
  echo "Shared data usage: processed2deployed-ryugu.sh shared [ aizu | stage | apl ] "
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

deployTarget="apl"
if [ $processingModelName = "shared" ]
then
  processingVersion="latest"
  if [ "$#" -gt 1 ]
  then
    deployTarget=$2;
  fi
else
  if [ "$#" -gt 2 ]
  then
    deployTarget=$3;
  fi
fi

echo "Processing Model Name: " $processingModelName
echo "Processing Version:    " $processingVersion
echo "Deployment Target:     " $deployTarget

# exit 0


rawTop="$pipelineTop/rawdata"
processedTop="$pipelineTop/processed"
deployedTop="/project/sbmt2/sbmt/data/bodies"

if [ $deployTarget = "aizu" ]
then
  deployedTop="sbmt@hyb2sbmt.u-aizu.ac.jp:/var/www/sbmt/sbmt/data"
elif [ $deployTarget = "stage" ]
then
  deployedTop="sbmt@hyb2sbmt.jhuapl.edu:/var/www/sbmt/sbmt/data"
fi

scriptDir="/project/sbmt2/sbmt/scripts"
importCmd="$scriptDir/import.sh"
rsyncCmd='rsync -rlptgDH --copy-links'

log="$rawTop/$bodyName/$processingVersion/logs/processed2deployed-ryugu.log"

srcTop="$processedTop/$bodyName"
destTop="$deployedTop/$bodyName"

echo "srcTop: $srcTop"
echo "destTop: $destTop"
echo "log: $log"

# exit 0

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

  if [ $deployTarget = "apl" ]
  then
    createDirIfNecessary $destTop/shared
  fi

#  echo Rsyncing "$srcTop/latest/shared/onc/imagelist*.txt" to $destTop/shared/onc/ # >> $log 2>&1
  doRsyncDir "$srcTop/latest/shared/onc/imagelist-info.txt" $destTop/shared/onc/
  doRsyncDir "$srcTop/latest/shared/onc/imagelist-fullpath-info.txt" $destTop/shared/onc/

  echo Rsyncing $srcTop/latest/shared/onc/infofiles/ to $destTop/shared/onc/infofiles/... # >> $log 2>&1
  doRsyncDir $srcTop/latest/shared/onc/infofiles/ $destTop/shared/onc/infofiles/

  echo Rsyncing $srcTop/latest/shared/onc/images/ to $destTop/shared/onc/images/... # >> $log 2>&1
  doRsyncDir $srcTop/latest/shared/onc/images/ $destTop/shared/onc/images/

  echo Rsyncing $srcTop/latest/shared/onc/gallery/ to $destTop/shared/onc/gallery/... # >> $log 2>&1
  doRsyncDir $srcTop/latest/shared/onc/gallery/ $destTop/shared/onc/gallery/

  echo Rsyncing $srcTop/latest/shared/tir/ to $destTop/shared/tir/... # >> $log 2>&1
  doRsyncDir $srcTop/latest/shared/tir/ $destTop/shared/tir/

  echo Rsyncing $srcTop/latest/shared/history/ to $destTop/shared/history/... # >> $log 2>&1
  doRsyncDir $srcTop/latest/shared/history/ $destTop/shared/history/

  # fix any bad permissions
  if [ $deployTarget = "apl" ]
  then
    echo $scriptDir/data-permissions.pl $destTop/shared
#    $scriptDir/data-permissions.pl $destTop/shared
  fi

else

  echo Rsyncing $srcTop/$processingVersion/$processingModelName to $destTop/$processingModelName-$processingVersion... # >> $log 2>&1
  doRsyncDir $srcTop/$processingVersion/$processingModelName $destTop/$processingModelName-$processingVersion

  # set the soft link to the shared onc directory
  if [ $deployTarget = "apl" ]
  then
    ln -s ../../shared/onc/images $destTop/$processingModelName-$processingVersion/onc/images

    # fix any bad permissions
    $scriptDir/data-permissions.pl $destTop/$processingModelName-$processingVersion
  fi

fi

echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
echo "Finished processed2deployed-ryugu.sh script"

