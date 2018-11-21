#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:      Russell Turner and Alex Welsh, updated by Josh Steele for Bennu
# Description:    Test version of script for transforming delivered Bennu data into
#                 rawdata format.  Data ONLY, not models
#-------------------------------------------------------------------------------

# Usage
#if [ "$#" -lt 1 ]
#then
#  echo "Data usage:  delivered2rawdata-bennu.sh"
#  exit 1
#fi

# Command line parameters
deliveredModelName="shared"
deliveredVersion="shared"

bodyName="bennu"

pipelineTop="/sbmt/pipeline"
deliveriesTop="/sbmt/deliveries"

deliveriesTop=$deliveriesTop
processingVersion="latest"
deliveredVersion="latest"

echo "Delivered version: " $deliveredVersion
echo "Delivered model name: " $deliveredModelName
echo "Processing version: " $processingVersion
#echo "Processing model name: " $processingModelName

rawTop="$pipelineTop/rawdata"
processedTop="$pipelineTop/processed"

scriptDir="/sbmt/scripts"
importCmd="$scriptDir/import.sh"
#rsyncCmd='rsync -rlptgDH --copy-links'
rsyncCmd='rsync -rltgDH --copy-links'

srcTop="$deliveriesTop/$bodyName"
destTop="$rawTop/$bodyName/$processingVersion"

echo "Source: $srcTop"
echo "Destination: $destTop"

releaseDir="$destTop"

logDir="$destTop/logs"
log="$logDir/delivery2rawdata.log"
echo "Log is $log"
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

echo "Starting deliveries2rawdata-bennu.sh script (log file: $log)"

makeLogDir

echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "Begin `date`" >> $log 2>&1

# Import everything from the delivery directory
createDirIfNecessary $destTop/shared
createDirIfNecessary $destTop/shared/ocams
createDirIfNecessary $destTop/shared/ocams/polycam
createDirIfNecessary $destTop/shared/ocams/polycam/images
createDirIfNecessary $destTop/shared/ocams/mapcam
createDirIfNecessary $destTop/shared/ocams/mapcam/images
createDirIfNecessary $destTop/shared/ocams/samcam
createDirIfNecessary $destTop/shared/ocams/samcam/images
#createDirIfNecessary $destTop/shared/spice
createDirIfNecessary $destTop/shared/otes
createDirIfNecessary $destTop/shared/otes/l2
createDirIfNecessary $destTop/shared/otes/l3
createDirIfNecessary $destTop/shared/ola
createDirIfNecessary $destTop/shared/ovirs/
createDirIfNecessary $destTop/shared/ovirs/l3/
createDirIfNecessary $destTop/shared/ovirs/l3/if
createDirIfNecessary $destTop/shared/ovirs/l3/reff

echo "Soft linking spice kernels due to large OLA ck"
#doRsyncDir $srcTop/spice/ $destTop/shared/spice/
cd $destTop/shared
echo "ln -s $srcTop/spice spice"
ln -s $srcTop/spice spice
echo "Soft linking complete"

declare -a sourceDirs=("$srcTop/ocams/mapcam" "$srcTop/ocams/polycam" "$srcTop/ocams/samcam" "$srcTop/otes/l2*" "$srcTop/otes/l3*" "$srcTop/ovirs/SA16l3escireff" "$srcTop/ola")
declare -a destDirs=("$destTop/shared/ocams/mapcam/images" "$destTop/shared/ocams/polycam/images" "$destTop/shared/ocams/samcam/images" "$destTop/shared/otes/l2" "$destTop/shared/otes/l3" "$destTop/shared/ovirs/l3/reff" "$destTop/shared/ola")

#declare -a sourceDirs=("$srcTop/ocams/mapcam" "$srcTop/ocams/polycam" "$srcTop/ocams/samcam" "$srcTop/otes/l2*" "$srcTop/otes/l3*" "$srcTop/ovirs/l3/if" "$srcTop/ovirs/l3/reff" "$srcTop/ola")
#declare -a destDirs=("$destTop/shared/ocams/mapcam/images" "$destTop/shared/ocams/polycam/images" "$destTop/shared/ocams/samcam/images" "$destTop/shared/otes/l2" "$destTop/shared/otes/l3" "$destTop/shared/ovirs/l3/if" "$destTop/shared/ovirs/l3/reff" "$destTop/shared/ola")

echo Rsyncing Files   
# copies over data files from the directories in sourceDirs and place the destDirs
index=0
echo ${sourceDirs[$index]}
for i in "${sourceDirs[@]}"
do
	for dir in $i 
	do
	   	echo "Dir is $dir, destdir is ${destDirs[$index]}"
	   	echo Rsyncing files from ${sourceDirs[$index]} to ${destDirs[$index]} ... >> $log 2>&1
#   echo Rsyncing files from ${sourceDirs[$index]} to ${destDirs[$index]} ...
#   for fileName in `ls $dir/*` # doRsync cannot handle *.fit so a seocnd for loop is needed to pass the specific name
#   do
      		doRsync $dir ${destDirs[$index]}
#   done
   		index=$((index + 1))
	done
done

<< --COMMENT--
echo fixing permissions
$scriptDir/data-permissions.pl $destTop/shared
   
echo removing unused files
#rm -rf $destTop/shared/*/images/*.tgz

--COMMENT--

echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
echo "Finished deliveries2rawdata-bennu.sh script"
