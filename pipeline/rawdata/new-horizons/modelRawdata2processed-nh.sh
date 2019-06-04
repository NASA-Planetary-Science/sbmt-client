#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:   Russell Turner and Alex Welsh, updated by Josh Steele for Bennu
#              and for New Horizons by James Peachey
# Description: Test version of script for transforming raw Bennu data into
#              processed format
#-------------------------------------------------------------------------------

missionShortName="nh"

# Usage
if [ "$#" -lt 4 ]
then
  echo "Model data usage:  rawdata2processed-$missionShortName.sh <body-name> <model-name> <processing-version> <processing-model-label>"
  exit 1
fi

# Command line parameters
bodyName=$1
rawdataModelName=$2
processingVersion=$3
processingModelLabel=$4
processingModelName=$rawdataModelName

if [ $processingModelName = "shared" ]
then
   echo "Error: this script doesn't process 'shared' data" >&2
   exit 1
fi

echo "Body name:$bodyName"
echo "Processing model name:$processingModelName"
echo "Processing version:$processingVersion"



pipelineTop="/project/sbmtpipeline"

rawdataTop="$pipelineTop/rawdata"
processedTop="$pipelineTop/processed"

scriptDir="/project/sbmt2/sbmt/scripts"
timeHistoryDir="$pipelineTop/timeHistory"
importCmd="$scriptDir/import.sh"
rsyncCmd='rsync -rlptgDH --copy-links'
briefPgrm="/project/nearsdc/software/spice/cspice/exe/brief"
javaCmd="/usr/bin/java"

srcTop="$rawdataTop/$bodyName/$processingVersion"
destTop="$processedTop/$bodyName/$processingVersion"

releaseDir="$srcTop"

# figures out what the latest kernel is for use in many of the processing methods
if [ $processingModelName = "shared" ]
then
   latestMKDir=$srcTop/shared/spice/kernels/mk
   #`ls $srcTop/shared/spice | sort -r | head -1`/kernels/mk
   latestKernel=`ls $latestMKDir/hyb2_analysis_v*.tm | sort -r | head -1`			########### FIX
   echo $latestMKDir
   echo $latestKernel
fi

logDir="$srcTop/logs"
log="$logDir/rawdata2processed.log"

# echo "Source Top: " $srcTop
# echo "Dest Top: " $destTop
# echo "Log file: " $log

#-------------------------------------------------------------------------------
# Reference to commonFuncs file, which contains funcs common to pipeline scripts
. $(dirname "$0")/commonFuncs.sh
#-------------------------------------------------------------------------------

# generates the imagelist-fullpath-sum.txt and imagelist-sum.txt files if SUM files are delivered.
processMakeSumfiles() {
  imageDir=$1
  instrument=`basename $imageDir`

  # write imagelist-fullpath-sum.txt
  if test -e $imageDir/imagelist-fullpath-sum.txt; then
     rm $imageDir/imagelist-fullpath-sum.txt
  fi
  fullpath=/$bodyName/$processingModelName/$instrument/images/
  awk '{print $8}' $imageDir/make_sumfiles.in | sed "s:^:$fullpath:" > $imageDir/imagelist-fullpath-sum.txt


  # write imagelist-sum.txt
  if test -e $imageDir/imagelist-sum.txt; then
     rm $imageDir/imagelist-sum.txt
  fi
  fileNames=`awk '{print $8" "}' $imageDir/make_sumfiles.in`
  for line in $fileNames
  do
     # extracts the time from the file name
     fileTime=`echo $line | sed 's/[^0-9]*\([0-9]\{4\}\)-\?\([0-9]\{2\}\)-\?\([0-9]\{2\}\)T\?\([0-9]\{2\}\)-\?\([0-9]\{2\}\)-\?\([0-9]\{2\}\).*/\1-\2-\3T\4:\5:\6/i'`
     echo "$line $fileTime" >> $imageDir/imagelist-sum.txt
  done
}

# edits the PATH SYMBOL in all the kernels in shared/spice/kernels/mk directory with the path to the metakernel
editKernel() {
  echo Editing metakernel to have correct path value
  for metaKernel in $latestMKDir/*.tm
  do
    echo Processing $metaKernel
    kernelsDir=`dirname $latestMKDir`
    sed -i "s#\(PATH_VALUES *= *\).*#\1( '$kernelsDir' ) #" $metaKernel
  done
  echo Finished editing metakernels
}

# Runs ls-pc.sh and DiscoverPlateColorings.sh, which is linked to a java tool.
discoverPlateColorings() {
  coloringDir=$destTop/$processingModelName/coloring
  if test `ls $coloringDir/coloring*.smd 2> /dev/null | wc -c` -eq 0; then
    "$scriptDir/ls-pc.sh" $coloringDir
    if test `grep -c . $coloringDir/../coloringlist.txt` -eq 0; then
      echo "No coloring files found in $coloringDir" >> $log 2>&1
      exit 1
    fi
    $releaseDir/sbmt/bin/DiscoverPlateColorings.sh $destTop/$processingModelName/coloring $bodyName/$processingModelName/coloring "$processingModelLabel/101955 Bennu" ../coloringlist.txt >> $log 2>&1
    if test $? -ne 0; then
      echo "Failed to generate plate coloring metadata" >> $log 2>&1
      exit 1
    fi
    rm -f $coloringDir/../coloringlist.txt* >> $log 2>&1
  else
    echo "File(s) coloring*.smd exist -- skipping generation of plate coloring metadata" >> $log 2>&1
  fi
}

#-------------------------------------------------------------------------------
# MAIN SCRIPT STARTS HERE.
#-------------------------------------------------------------------------------

if test `whoami` = sbmt; then
  echo "Run this script while logged in as yourself." >&2
  exit 1
fi

# Confirm the source directory points somewhere.
if test ! -d $srcTop/$rawdataModelName; then
  echo "Error: source directory $srcTop/$rawdataModelName does not exist" >&2
  exit 1
fi

# Do everything in the rawdata directory except as required.
cd $srcTop/$rawdataModelName
if test $? -ne 0; then
  echo "Unable to cd $srcTop/$rawdataModelName. Cannot continue" >&2
  exit 1
fi

echo "Starting rawdata2processed-$missionShortName.sh script (log file: $log)"
mkdir -p $destTop

makeLogDir

echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "Begin `date`" >> $log 2>&1

   createDirIfNecessary $destTop/$processingModelName/shape

   # Process the shape models.
   doRsyncDirIfNecessary $srcTop/$rawdataModelName/shape/ $destTop/$processingModelName/shape/
   doGzipDirIfNecessary $destTop/$processingModelName/shape

   # Process delivered sumfiles
   for imager in lorri leisa mvic; do
     if [ -d "$srcTop/$rawdataModelName/$imager" ]
     then
       echo Beginning sumfile processing
       createDirIfNecessary $destTop/$processingModelName/$imager

       # generates imagelist-sum.txt and imagelist-fullpath.txt
       processMakeSumfiles $srcTop/$rawdataModelName/$imager

       # copies over imager directory
       doRsync $srcTop/$rawdataModelName/$imager $destTop/$processingModelName/$imager

       echo Finished $imager processing
   fi
   done

   # process coloring data and generates the metadata needed to read the coloring data.
   if [ -d "$srcTop/$rawdataModelName/coloring" ]
   then
      echo Processing plate colorings
      createDirIfNecessary $destTop/$processingModelName/coloring
      doRsync $srcTop/$rawdataModelName/coloring/ $destTop/$processingModelName/coloring/

      # gzips the coloring files
      doGzipDir $destTop/$processingModelName/coloring

      # runs James' java tool, DiscoverPlateColorings, that class ues an intermediate script, ls-pc.sh which is located in /project/sbmt2/sbmt/scripts
      discoverPlateColorings
      echo finished processing plate colorings
   fi

   # process DTMs.
   if [ -d "$srcTop/$rawdataModelName/dtm" ]
   then
      echo Processing DTMs
      createDirIfNecessary $destTop/$processingModelName/dtm/browse
      doRsync $srcTop/$rawdataModelName/dtm/ $destTop/$processingModelName/dtm/browse/
      
      (cd $destTop/$processingModelName/dtm/browse; ls | sed 's:\(.*\):\1\,\1:' | grep -v 'fileList.txt' > fileList.txt)    
   fi

   # fix any bad permissions
   echo Correcting permissions >> $log 2>&1
   $scriptDir/data-permissions.pl $destTop/$processingModelName



echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
echo "Finished rawdata2processed-$missionShortName.sh script"

