#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:      Russell Turner and Alex Welsh, updated by Josh Steele for Bennu
# Description:    Test version of script for transforming delivered Bennu data into
#                 rawdata format, model data ONLY, no shared data
#-------------------------------------------------------------------------------

# Usage
if [ "$#" -lt 1 ]
then
  echo "Model data usage:  delivered2rawdata-bennu.sh <delivered-model-name> <delivered-version> [ <processed-model-name> <processing-version> ]"
  exit 1
fi

# Command line parameters
deliveredModelName=$1
deliveredVersion=$2
processingModelName=$3
processingVersion=$4

bodyName="bennu"

pipelineTop="/sbmt/pipeline"
deliveriesTop="/sbmt/deliveries"

echo "Delivered version: " $deliveredVersion
echo "Delivered model name: " $deliveredModelName
echo "Processing version: " $processingVersion
echo "Processing model name: " $processingModelName

rawTop="$pipelineTop/rawdata"
processedTop="$pipelineTop/processed"

scriptDir="/sbmt/scripts"
importCmd="$scriptDir/import.sh"
#rsyncCmd='rsync -rlptgDH --copy-links'
rsyncCmd='rsync -rltgDH --copy-links'

srcTop="$deliveriesTop/$bodyName/$deliveredVersion"
destTop="$rawTop/$bodyName/$processingVersion"

releaseDir="$destTop"

logDir="$destTop/logs"
log="$logDir/delivery2rawdata.log"

#-------------------------------------------------------------------------------
# Reference to commonFuncs file, which contains funcs common to pipeline scripts
. $(dirname "$0")/commonFuncs.sh
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# MAIN SCRIPT STARTS HERE.
#-------------------------------------------------------------------------------

if test `whoami` = sbmt; then
  echo "Run this script while logged in as yourself." >&2
  exit 1
fi

# Confirm the source directory points somewhere for MODELS
if [ $deliveredModelName != "shared" ]
then
	if test ! -d $srcTop/$deliveredModelName; then
	  echo "Error: source directory $srcTop/$deliveredModelName does not exist" >&2
	  exit 1
	fi
fi

export SBMTROOT="$releaseDir/sbmt"
export SAAVTKROOT="$releaseDir/saavtk"

echo "Starting deliveries2rawdata-bennu.sh script (log file: $log)"

makeLogDir

echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "Begin `date`" >> $log 2>&1

# Check out and build code.
#checkoutCodeIfNecessary
#if test $? -ne 0; then
#  exit 1
#fi
#buildCodeIfNecessary
#if test $? -ne 0; then
#  exit 1
#fi


# Import everything from the delivery directory
	
manifest="aamanifest.txt"
if test ! -f $srcTop/$deliveredModelName/$manifest; then
  	echo "No manifest $srcTop/$deliveredModelName/$manifest" >> $log 2>&1
  	exit 1
fi

if test ! -d $destTop/$processingModelName; then
  	mkdir -p $destTop/$processingModelName
  	if test $? -ne 0; then
    	echo "Cannot create destination directory $destTop/$processingModelName" >> $log 2>&1
    	exit 1
  	fi
fi

# Confirm the source directory points somewhere.
if test ! -d $srcTop/$deliveredModelName; then
  echo "Error: directory $srcTop/$deliveredModelName does not exist" >&2
  exit 1
fi

   #echo $srcTop/$deliveredModelName/ Rawdata: $destTop/$processingModelName/ #*** don't uncomment old code ***
   #doRsyncDir $srcTop/$deliveredModelName/* $destTop/$processingModelName/ #*** don't uncomment old code ***


# otherwise, assume this is model data and copy over into a new model
createDirIfNecessary $destTop/$processingModelName/shape

# copy the manifest as a backup
doRsync $srcTop/$deliveredModelName/aamanifest.txt $destTop/$processingModelName/aamanifest.txt

# Define a series of directories to copy
declare -s dirsToCopy=("coloring" "imaging" "ocams" "ola" "otes" "ovirs")

# copy the shape model
doRsyncDir $srcTop/$deliveredModelName/shape $destTop/$processingModelName/shape
   
# Copy coloring files
#doRsyncDirIfNecessary $srcTop/$deliveredModelName/coloring $destTop/$processingModelName/coloring

for dir in ${dirsToCopy[@]}
do
	if [ -d "$srcTop/$deliveredModelName/$dir" ]
	then
  		createDirIfNecessary $destTop/$processingModelName/$dir
  		# copy the files
  		doRsyncDir $srcTop/$deliveredModelName/$dir $destTop/$processingModelName/$dir
	fi
done

echo fixing permissions
$scriptDir/data-permissions.pl $destTop/$processingModelName

echo removing unused files
#rm -rf $destTop/shared/*/images/*.tgz
#rm -rf $destTop/shared/*/images/*.d
#rm -rf $destTop/shared/*/images/index.html*

echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
echo "Finished modelDdelivery2rawdata-bennu.sh script"
