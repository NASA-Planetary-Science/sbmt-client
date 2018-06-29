#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:   Russell Turner and Alex Welsh
# Redmine:     sbmt1dev issue #1304
# Description: Test version of script for transforming raw JAXA data into
#              processed format
#-------------------------------------------------------------------------------
# Note: bodyName, deliveredVersion, rawdataModelName, processingVersion and processingModelName need to be configured for each model

pipelineTop="/project/sbmtpipeline"
bodyName="ryugu"

rawdataModelName="jaxa-001"
if [ "$#" -gt 0 ]
then
  rawdataModelName=$1
fi

processingVersion="20180628"
if [ "$#" -gt 1 ]
then
  processingVersion=$2
fi

processingModelName=$rawdataModelName

echo "Body name: " $bodyName
echo "Processing model name: " $processingModelName
echo "Processing version: " $processingVersion

legacyTop="/project/sbmt2/sbmt/nearsdc/data"
deliveriesTop="$pipelineTop/deliveries"
rawdataTop="$pipelineTop/rawdata"
processedTop="$pipelineTop/processed"
deployedTop="/project/sbmt2/sbmt/data/bodies"

scriptDir="/project/sbmt2/sbmt/scripts"
importCmd="$scriptDir/import.sh"
rsyncCmd='rsync -rlptgDH --copy-links'
log="logs/rawdata2processed-model.log"

srcTop="$rawdataTop/$bodyName/$processingVersion"
destTop="$processedTop/$bodyName/$processingVersion"
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

# Process images/sumfiles from source to destination.
processImager() (
  srcImager="$1"
  if test "x$srcImager" = x; then
    echo "processImager: missing source imager (directory) argument." >> $log 2>&1
    exit 1
  fi

  srcImageList="$2"
  if test "x$srcImageList" = x; then
    echo "processImager: missing source image list directory argument." >> $log 2>&1
    exit 1
  fi

  destImager="$3"
  if test "x$destImager" = x; then
    echo "processImager: missing destination imager (directory) argument." >> $log 2>&1
    exit 1
  fi

  destImagePrefix="$4"
  if test "x$destImagePrefix" = x; then
    echo "processImager: missing destination image prefix argument." >> $log 2>&1
    exit 1
  fi

  srcImageDir="$srcImager/images"
  if test ! -d $srcImageDir; then
    echo "processImager: missing source images/ directory $srcImageDir" >> $log 2>&1
    exit 1
  fi

  srcSumFileDir="$srcImager/sumfiles"
  if test ! -d $srcSumFileDir; then
    srcSumFileDir="$srcImager/sumfiles-corrected"
    if test ! -d $srcSumFileDir; then
      echo "processImager: cannot find source sumfile directory $srcImager/sumfiles OR $srcSumFileDir" >> $log 2>&1
      exit 1
    fi
  fi

  srcGalleryDir="$srcImager/gallery"

  destImageDir="$destImager/images"
  destSumFileDir="$destImager/sumfiles"
  destImageList="$destImager/imagelist.txt"
  destImageListFullPath="$destImager/imagelist-fullpath.txt"
  destGalleryDir="$destImager/gallery"

  for fileToMatch in `cat $srcImageList | sed -e 's:.*/::'`; do
    imageFileRoot=`echo $fileToMatch | sed -e 's:\.fits\?$::i'`
    srcImageFile=`ls $srcImageDir/$fileToMatch 2> /dev/null`
    if test "x$srcImageFile" != x; then
      srcSumFileRoot=$imageFileRoot
      srcSumFile=`ls $srcSumFileDir/$srcSumFileRoot.* 2> /dev/null`
      if test "x$srcSumFile" = x; then
        srcSumFileRoot=`echo $imageFileRoot | sed -e 's:_::g'`
        srcSumFile=`ls $srcSumFileDir/$srcSumFileRoot.* 2> /dev/null`
      fi
      if test "x$srcSumFile" != x; then

        # Found a match.
        destSumFileRoot=$imageFileRoot
        destImageFile="$destImageDir/$imageFileRoot.FIT"
        destSumFile="$destSumFileDir/$destSumFileRoot.SUM"
  
        destDir=`echo $destImageFile | sed -e 's:[^/][^/]*$::'`
        createDirIfNecessary $destDir
        doRsync $srcImageFile $destImageFile
  
        destDir=`echo $destSumFile | sed -e 's:[^/][^/]*$::'`
        createDirIfNecessary $destDir
        doRsync $srcSumFile $destSumFile
  
        srcGalleryFiles=`ls $srcGalleryDir/$imageFileRoot.* 2> /dev/null`
        if test "x$srcGalleryFiles" != x; then
          createDirIfNecessary $destGalleryDir
          for file in `echo $srcGalleryFiles`; do
            doRsync $file $destGalleryDir
          done
        fi

        echo $imageFileRoot.FIT >> $destImageList
        echo "$destImagePrefix/$imageFileRoot.FIT" >> $destImageListFullPath
      else
        echo "processImager: could not find sumfile for $fileToMatch" >> $log 2>&1
      fi
    else
      echo "processImager: could not find image matching $fileToMatch" >> $log 2>&1
    fi
  done
)

generateInfoFiles() (
 CMD="$srcTop//$rawdataModelName/onc/generateInfoFiles.sh $1 $2 $3 $4"
 $CMD   
)

#-------------------------------------------------------------------------------
# MAIN SCRIPT STARTS HERE.
#-------------------------------------------------------------------------------

echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "Begin `date`" >> $log 2>&1

echo "Source Top: " $srcTop/$processingModelName
echo "Dest Top: " $destTop/$processingModelName

createDirIfNecessary $destTop/$processingModelName/shape
createDirIfNecessary $destTop/$processingModelName/coloring

doRsync $srcTop/$rawdataModelName/shape/ $destTop/$processingModelName/shape/
gzip -f $destTop/$processingModelName/shape/*.obj

doRsync $srcTop/$rawdataModelName/coloring/ $destTop/$processingModelName/coloring/
gzip -f $destTop/$processingModelName/coloring/*.fits

# echo $srcTop/$rawdataModelName/onc/ $destTop/$processingModelName/onc
# createDirIfNecessary $destTop/$processingModelName/onc
# doRsync $srcTop/$rawdataModelName/onc/ $destTop/$processingModelName/onc
# generateInfoFiles $srcTop/$rawdataModelName $srcTop/$rawdataModelName $srcTop/$rawdataModelName/spice/kernels/mk/hyb2_lss_truth.tm RYUGU

# fix any bad permissions
$scriptDir/data-permissions.pl $destTop/$processingModelName                                      

echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
