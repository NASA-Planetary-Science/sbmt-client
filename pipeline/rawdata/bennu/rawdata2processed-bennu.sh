#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:   Russell Turner and Alex Welsh, updated by Josh Steele for Bennu
# Description: Test version of script for transforming raw Bennu data into
#              processed format, shared data only, NO MODELS
#-------------------------------------------------------------------------------

# Usage
if [ "$#" -lt 1 ]
then
  echo "Shared data usage: rawdata2processed-bennu.sh shared"
  exit 1
fi

# Command line parameters
rawdataModelName=$1
processingModelName=$rawdataModelName
processingVersion="latest"                                                      

bodyName="bennu"
bodyNameCaps="BENNU"
spacecraftName="ORX"

echo "Body name:$bodyName"
echo "Processing model name:$processingModelName"
echo "Processing version:$processingVersion"

pipelineTop="/sbmt/pipeline"

rawdataTop="$pipelineTop/rawdata"
processedTop="$pipelineTop/processed"

scriptDir="/sbmt/scripts"
timeHistoryDir="$pipelineTop/timeHistory"
importCmd="$scriptDir/import.sh"
#rsyncCmd='rsync -rlptgDH --copy-links'
rsyncCmd='rsync -rltgDH --copy-links'
briefPgrm="/export/altwg/otherSoftware/cspice/exe/brief"
javaCmd="/usr/bin/java"

srcTop="$rawdataTop/$bodyName/$processingVersion"
destTop="$processedTop/$bodyName/$processingVersion"

releaseDir="$srcTop"

# figures out what the latest kernel is for use in many of the processing methods
latestMKDir=$srcTop/shared/spice/kernels/flight/mk
#`ls $srcTop/shared/spice | sort -r | head -1`/kernels/mk
latestKernel=`ls $latestMKDir/spoc-digest-*.mk | sort -r | head -1`	
echo $latestMKDir
echo $latestKernel

logDir="$srcTop/logs"
log="$logDir/rawdata2processed.log"

# echo "Source Top: " $srcTop                                                                       
# echo "Dest Top: " $destTop                                                                        
# echo "Log file: " $log

#-------------------------------------------------------------------------------
# Reference the commonFuncs file 
. $(dirname "$0")/commonFuncs.sh
#-------------------------------------------------------------------------------

# Process images/sumfiles from source to destination.
# Arguments: src-imaging-directory src-image-list-file dest-imaging-directory dest-prefix
#            (First three are full paths, last one is the string stuck in before the
#            file name in the output imagelist-fullpath.txt file).
processImager() {
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
  destImageList="$destImager/imagelist-sum.txt"
  destImageListFullPath="$destImager/imagelist-fullpath-sum.txt"
  destGalleryDir="$destImager/gallery"

  for fileToMatch in `cat $srcImageList | sed 's:.*/::'`; do
    imageFileRoot=`echo $fileToMatch | sed 's:\.fits\?$::i'`
    srcImageFile=`ls $srcImageDir/$fileToMatch 2> /dev/null`
    if test "x$srcImageFile" != x; then
      srcSumFileRoot=$imageFileRoot
      srcSumFile=`ls $srcSumFileDir/$srcSumFileRoot.* 2> /dev/null`
      if test "x$srcSumFile" = x; then
        srcSumFileRoot=`echo $imageFileRoot | sed 's:_::g'`
        srcSumFile=`ls $srcSumFileDir/$srcSumFileRoot.* 2> /dev/null`
      fi
      if test "x$srcSumFile" != x; then

        # Found a match.
        destSumFileRoot=$imageFileRoot
        destImageFile="$destImageDir/$imageFileRoot.FIT"
        destSumFile="$destSumFileDir/$destSumFileRoot.SUM"

        destDir=`echo $destImageFile | sed 's:[^/][^/]*$::'`
        createDirIfNecessary $destDir
        doRsync $srcImageFile $destImageFile

        destDir=`echo $destSumFile | sed 's:[^/][^/]*$::'`
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

  if test -d $destImageDir; then
    chmod -x $destImageDir/*
  fi
  if test -d $destSumFileDir; then
    chmod -x $destSumFileDir/*
  fi
  if test -d $destGalleryDir; then
    chmod -x $destGalleryDir/*
  fi

  removeDuplicates $destImageList
  removeDuplicates $destImageListFullPath
}

# Copies over the files from rawdata and amkes fileList.txt file containing body relative path followed by start and stop time of track in ET
processLidarBrowse() {
	lidarDir=$1
	if test -e $lidarDir/fileList.txt; then
     		rm $lidarDir/fileList.txt
  	fi
  	
  	if test -e $lidarDir/listing.txt; then
     		rm $lidarDir/listing.txt
  	fi
  	
  	listingCMD=`ls -1 $lidarDir/*.csv > $lidarDir/listing.txt`
  	$listingCMD
  	
	CMD="$scriptDir/create_lidar_filelist $lidarDir/listing.txt $lidarDir/fileList.txt $latestKernel $bodyName"
  	$CMD 
}

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
     fileTime=`echo $line | sed 's:[^_]*_[^_]*_::' | sed 's:\(.*[0-9][0-9][0-9][0-9]\).*:\1:' |
     sed 's:\(..\)$:\:\1:' | sed 's:\(..\:\):\:\1:' | sed 's:_:T:' | sed 's:\(..T\):-\1:' | sed 's:\(..-\):-\1:'`
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

# called the generateInfoFiles.sh script in /project/sbmt2/sbmt/scripts and generates the INFO files, that script also uses creat_info_files, located in the scripts dir as well.
generateInfoFiles() {
  CMD="$scriptDir/generateInfoFiles.sh $1 $2 $3 $4 $5 $6"
  $CMD   
}

# generates the CVS time history (later removed by this script) and the final timeHistory.bth
# the c++ code that generates the csv is located in projects/sbmtpipeline/timeHistory. there are 3 scripts, getFov.c getSpacecraftState.c and getTargetState.c 
# had to be modified specifically for Ryugu (IAU_RYUGU_FIXED). If those need to be edited, do so and run comple.c (in the timeHistory directory) and a new c++ program will be generated
# the ConvertCSVToBinary.class is located in /project/sbmt2/sbmt/scripts 
# Lillian can have more information
generateTimeHistory() {
  # Finds the appropriate start and endTime and generates the .csv file
  startTime=`$briefPgrm -t $latestKernel | grep $spacecraftName | head -1 | cut -f 3-7 -d' '`
  echo "$briefPgrm -t $latestKernel"
  #endTime=`$briefPgrm -t $latestKernel | grep $spacecraftName | tail -n 23 | head -n 1 | sed 's/^.*2022/2022/'`
  endTime=`$briefPgrm -t $latestKernel | grep BENNU | tail -n 1| sed 's/^.*2022/2022/'`
  echo Start Time: $startTime
  echo End Time: $endTime

  # makes the CSV files in the rawdata/bennu directory, takes 5 parameters [body-name] [scpacecraft-name] [latest-kernel] [startTime] [endTime] [optional-rotation]
  makeTimeHistory='$timeHistoryDir/timeHistory $bodyNameCaps $spacecraftName $latestKernel "$startTime" "$endTime"'
  eval $makeTimeHistory
  echo before move
  mv $spacecraftName\_$bodyNameCaps\_timeHistory.csv $destTop/shared/history
  echo Made time History

  # removes existing timeHistory.bth file and generates a new one
  if test -e $destTop/shared/history/timeHistory.bth
  then 
     rm $destTop/shared/history/timeHistory.bth
     echo Removed existing timeHistory.bth file
  fi
  
  # generates timeHistory.bth, takes 2 parameters [source-of-.CSV] [destination-of-.bth]
  convertToBinary="$javaCmd -cp $scriptDir/ ConvertCSVToBinary $destTop/shared/history/$spacecraftName\_$bodyNameCaps\_timeHistory.csv $destTop/shared/history/timeHistory.bth"
  eval $convertToBinary
  echo Done converting to binary file
}

# makes the gallery using 2 scripts, fits2thumbnails.py and make_gallery_webpage.py, both located in /project/sbmt2/sbmt/scripts
generateGallery() {
  echo Start gallery generation
  F2T="python $scriptDir/fits2thumbnails.py $2 $3"
  makeWebpage="python $scriptDir/make_gallery_webpage.py $1 $2 $3 $4 $5"
  $F2T
  $makeWebpage
  echo End gallery generation
}

# Runs ls-pc.sh and DiscoverPlateColorings.sh, which is linked to a java tool.
discoverPlateColorings() {
  coloringDir=$destTop/$processingModelName/coloring
  if test `ls $coloringDir/coloring*.smd 2> /dev/null | wc -c` -eq 0; then
    "$scriptDir/ls-pc.sh" $coloringDir
    if test `grep -c . $coloringDir/coloringlist.txt` -eq 0; then
      echo "No coloring files found in $coloringDir" >> $log 2>&1
      exit 1
    fi
    $releaseDir/sbmt/bin/DiscoverPlateColorings.sh $destTop/$processingModelName/coloring $bodyName/$processingModelName/coloring "$modelName/101955 Bennu" >> $log 2>&1
    if test $? -ne 0; then
      echo "Failed to generate plate coloring metadata" >> $log 2>&1
      exit 1
    fi
    rm -f $destTop/$processingModelName/coloring/coloringlist.txt* >> $log 2>&1
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

echo "Starting rawdata2processed-bennu.sh script (log file: $log)"
mkdir -p $destTop                                                                                 
                        
makeLogDir

echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "Begin `date`" >> $log 2>&1

echo "Creating destination directories if needed....."
# if the model name is "shared" then we only copy over the new shared data
createDirIfNecessary $destTop/shared
createDirIfNecessary $destTop/shared/ocams
createDirIfNecessary $destTop/shared/ocams/polycam
createDirIfNecessary $destTop/shared/ocams/polycam/images
createDirIfNecessary $destTop/shared/ocams/polycam/infofiles
createDirIfNecessary $destTop/shared/ocams/polycam/gallery
createDirIfNecessary $destTop/shared/ocams/mapcam
createDirIfNecessary $destTop/shared/ocams/mapcam/images
createDirIfNecessary $destTop/shared/ocams/mapcam/infofiles
createDirIfNecessary $destTop/shared/ocams/mapcam/gallery
createDirIfNecessary $destTop/shared/ocams/samcam
createDirIfNecessary $destTop/shared/ocams/samcam/images
createDirIfNecessary $destTop/shared/ocams/samcam/infofiles
createDirIfNecessary $destTop/shared/ocams/samcam/gallery
createDirIfNecessary $destTop/shared/history
createDirIfNecessary $destTop/shared/spice
createDirIfNecessary $destTop/shared/otes
createDirIfNecessary $destTop/shared/otes/l2
createDirIfNecessary $destTop/shared/otes/l3
createDirIfNecessary $destTop/shared/otes/hypertree
createDirIfNecessary $destTop/shared/ola/
createDirIfNecessary $destTop/shared/ola/browse
createDirIfNecessary $destTop/shared/ola/hypertree
createDirIfNecessary $destTop/shared/ovirs/
createDirIfNecessary $destTop/shared/ovirs/hypertree
createDirIfNecessary $destTop/shared/ovirs/l3/
createDirIfNecessary $destTop/shared/ovirs/l3/if
createDirIfNecessary $destTop/shared/ovirs/l3/reff

# edits metakernel so that the correct path value is write in the metakernel file
# editKernel 

<< --COMMENT2--

######### POLYCAM ############
echo "Processing POLYCAM"
doRsyncDir $srcTop/shared/ocams/polycam/images $destTop/shared/ocams/polycam/images
# remove extraneous html files
rm -f $destTop/shared/ocams/polycam/images/index.html*
# generates info files for POLYCAM
# takes 6 parameters, [source-dir] [dest-dir] [most-updated-kernel] [body-name-uppercase] [camera-name-informal] [camera-name-formal]
echo "Generating INFO files"
echo "generateInfoFiles $srcTop/shared/ocams $destTop/shared/ocams $latestKernel $bodyNameCaps polycam ORX_OCAMS_POLYCAM"
generateInfoFiles $srcTop/shared/ocams $destTop/shared/ocams EMBED $bodyNameCaps polycam ORX_OCAMS_POLYCAM >> $log 2>&1
sed -i 's/latest\///g' $destTop/shared/ocams/polycam/imagelist-fullpath-info.txt
# generates gallery for POLYCAM
# takes 5 parameters [*.fileExtension] [image-source] [gallery-destination] [body-name-lowercase] [path-of-imagelist-info.txt]
generateGallery "*.fit" $srcTop/shared/ocams/polycam/images $destTop/shared/ocams/polycam/gallery $bodyName $destTop/shared/ocams/polycam/imagelist-info.txt

######### MAPCAM ############
echo "Processing MAPCAM"
doRsyncDir $srcTop/shared/ocams/mapcam/images $destTop/shared/ocams/mapcam/images
# remove extraneous html files
rm -f $destTop/shared/ocams/mapcam/images/index.html*
# generates info files for MAPCAM
# takes 6 parameters, [source-dir] [dest-dir] [most-updated-kernel] [body-name-uppercase] [camera-name-informal] [camera-name-formal]
echo "Generating INFO files"
echo "generateInfoFiles $srcTop/shared/ocams $destTop/shared/ocams EMBED $bodyNameCaps mapcam ORX_OCAMS_MAPCAM"
generateInfoFiles $srcTop/shared/ocams $destTop/shared/ocams EMBED $bodyNameCaps mapcam ORX_OCAMS_MAPCAM
sed -i 's/latest\///g' $destTop/shared/ocams/mapcam/imagelist-fullpath-info.txt
# generates gallery for MAPCAM
# takes 5 parameters [*.fileExtension] [image-source] [gallery-destination] [body-name-lowercase] [path-of-imagelist-info.txt]
generateGallery "*.fit" $srcTop/shared/ocams/mapcam/images $destTop/shared/ocams/mapcam/gallery $bodyName $destTop/shared/ocams/mapcam/imagelist-info.txt

######### SAMCAM ############
echo "Processing SAMCAM"
doRsyncDir $srcTop/shared/ocams/samcam/images $destTop/shared/ocams/samcam/images
# remove extraneous html files
rm -f $destTop/shared/ocams/samcam/images/index.html*
# generates info files for SAMCAM
# takes 6 parameters, [source-dir] [dest-dir] [most-updated-kernel] [body-name-uppercase] [camera-name-informal] [camera-name-formal]
echo "Generating INFO files"
echo "generateInfoFiles $srcTop/shared/ocams $destTop/shared/ocams EMBED $bodyNameCaps samcam ORX_OCAMS_SAMCAM"
generateInfoFiles $srcTop/shared/ocams $destTop/shared/ocams EMBED $bodyNameCaps samcam ORX_OCAMS_SAMCAM
sed -i 's/latest\///g' $destTop/shared/ocams/samcam/imagelist-fullpath-info.txt
# generates gallery for SAMCAM
# takes 5 parameters [*.fileExtension] [image-source] [gallery-destination] [body-name-lowercase] [path-of-imagelist-info.txt]
generateGallery "*.fit" $srcTop/shared/ocams/samcam/images $destTop/shared/ocams/samcam/gallery $bodyName $destTop/shared/ocams/samcam/imagelist-info.txt

--COMMENT2--

######### TIMEHISTORY ######
echo "Processing TIMEHISTORY"
# deletes existing timeHistory files and generates a new one
generateTimeHistory


<< --COMMENT--

######### OVIRS ############
echo "Processing OVIRS"
doRsyncDir $srcTop/shared/ovirs $destTop/shared/ovirs
generateInfoFiles $srcTop/shared $destTop/shared $latestKernel $bodyNameCaps ovirs ORX_OVIRS
sed -i 's/latest\///g' $destTop/shared/ovirs/spectrumlist-fullpath-info.txt

######### OTES  ############
echo "Processing OTES"
doRsyncDir $srcTop/shared/otes $destTop/shared/otes
# takes 6 parameters, [source-dir] [dest-dir] [most-updated-kernel] [body-name-uppercase] [camera-name-informal] [camera-name-formal]
generateInfoFiles $srcTop/shared $destTop/shared $latestKernel $bodyNameCaps otes ORX_OTES
sed -i 's/latest\///g' $destTop/shared/otes/spectrumlist-fullpath-info.txt

#########  OLA  ############
echo "Processing OLA"
# copy over OLA data from rawdata to processed
doRsyncDir $srcTop/shared/ola/browse $destTop/shared/ola/browse
   
# generates fileList for LIDAR
# takes 1 parameters [lidar-sourcefile]
processLidarBrowse $destTop/shared/ola/browse/
sed -i 's/latest\///g' $destTop/shared/ola/browse/fileList.txt

############ CLEANUP #############
echo "Cleaning up"   
# removed extra files that were generated and the time history csv (not the final timeHistory.bth product)
rm -rf $destTop/shared/ocams/polycam/*Files.txt
rm -rf $destTop/shared/ocams/mapscam/*Files.txt
rm -rf $destTop/shared/ocams/samcam/*Files.txt
rm -rf $destTop/shared/history/$spacecraftName*
   
echo Correcting permissions >> $log 2>&1
$scriptDir/data-permissions.pl $destTop/shared

--COMMENT--

echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
echo "Finished rawdata2processed-bennu.sh script"
