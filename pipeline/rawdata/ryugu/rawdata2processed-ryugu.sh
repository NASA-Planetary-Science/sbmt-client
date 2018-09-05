#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:   Russell Turner and Alex Welsh
# Redmine:     sbmt1dev issue #1304
# Description: Test version of script for transforming raw JAXA data into
#              processed format
#-------------------------------------------------------------------------------

# Usage
if [ "$#" -lt 1 ]
then
  echo "Model data usage:  rawdata2processed-ryugu.sh <model-name> <processing-version>"
  echo "Shared data usage: rawdata2processed-ryugu.sh shared"
  exit 1
fi

# Command line parameters
rawdataModelName=$1
processingVersion=$2
processingModelName=$rawdataModelName

if [ $processingModelName = "shared" ]
then                                                                                              
   processingVersion="latest"
fi                                                                                                
                                                                                                  
echo "Body name:$bodyName"
echo "Processing model name:$processingModelName"
echo "Processing version:$processingVersion"


bodyName="ryugu"
bodyNameCaps="RYUGU"
spacecraftName="HAYABUSA2"

pipelineTop="/project/sbmtpipeline"

rawdataTop="$pipelineTop/rawdata"
processedTop="$pipelineTop/processed"

scriptDir="/project/sbmt2/sbmt/scripts"
timeHistoryDir="$pipelineTop/timeHistory"
importCmd="$scriptDir/import.sh"
#rsyncCmd='rsync -rlptgDH --copy-links'
rsyncCmd='rsync -rltgDH --copy-links'
briefPgrm="/project/nearsdc/software/spice/cspice/exe/brief"
javaCmd="/project/nearsdc/software/java/x86_64/latest/bin/java"

srcTop="$rawdataTop/$bodyName/$processingVersion"
destTop="$processedTop/$bodyName/$processingVersion"

releaseDir="$srcTop/$processingModelName"

# figures out what the latest kernel is for use in many of the processing methods
if [ $processingModelName = "shared" ]
then
   latestMKDir=$srcTop/shared/spice/kernels/mk
   #`ls $srcTop/shared/spice | sort -r | head -1`/kernels/mk
   latestKernel=`ls $latestMKDir/hyb2_analysis_v*.tm | sort -r | head -1`
   echo $latestMKDir
   echo $latestKernel
fi 

logDir="$srcTop/logs"
log="$logDir/rawdata2processed.log"

# echo "Source Top: " $srcTop                                                                       
# echo "Dest Top: " $destTop                                                                        
# echo "Log file: " $log

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

removeDuplicates() {
  file=$1
  if test "x$file" = x; then
    echo "removeDuplicates was called but missing its argument, the file from which to remove duplicates." >> $log 2>&1
    exit 1
  fi
  if test ! -f $file; then
    echo "removeDuplicates: file $file does not exist." >> $log 2>&1
    exit 1
  fi
  mv $file $file.in
  if test $? -ne 0; then
    echo "removeDuplicates: unable to rename file $file." >> $log 2>&1
    exit 1
  fi
  sort -u $file.in > $file
  if test $? -ne 0; then
    echo "removeDuplicates: unable to remove duplicate lines from file $file." >> $log 2>&1
    exit 1
  fi
  rm -f $file.in
}

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
  	
	CMD="$scriptDir/create_lidar_filelist $lidarDir/listing.txt $lidarDir/fileList.txt $latestKernel"
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
  endTime=`$briefPgrm -t $latestKernel | grep $spacecraftName | tail -1 | sed 's/^.*2018/2018/'`
  echo Start Ttime: $startTime
  echo End Time: $endTime

  # makes the CSV files in the rawdata/ryugu directory, takes 5 parameters [body-name] [scpacecraft-name] [latest-kernel] [startTime] [endTime] [optional-rotation]
  makeTimeHistory='$timeHistoryDir/timeHistory $bodyNameCaps $spacecraftName $latestKernel "$startTime" "$endTime"'
  eval $makeTimeHistory
  echo before move
  mv $spacecraftName\_$bodyNameCaps\_timeHistory.csv $destTop/shared/"history"
  echo Made time History

  # removes existing timeHistory.bth file and generates a new one
  if test -e $destTop/shared/"history"/timeHistory.bth
  then 
     rm $destTop/shared/"history"/timeHistory.bth
     echo Removed existing timeHistory.bth file
  fi
  
  # generates timeHistory.bth, takes 2 parameters [source-of-.CSV] [destination-of-.bth]
  convertToBinary="$javaCmd -cp $scriptDir/ ConvertCSVToBinary $destTop/shared/"history"/$spacecraftName\_$bodyNameCaps\_timeHistory.csv $destTop/shared/"history"/timeHistory.bth"
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
    $releaseDir/sbmt/bin/DiscoverPlateColorings.sh $destTop/$processingModelName/coloring $bodyName/$processingModelName/coloring "$modelName/162173 Ryugu" >> $log 2>&1
    if test $? -ne 0; then
      echo "Failed to generate plate coloring metadata" >> $log 2>&1
      exit 1
    fi
    rm -f $destTop/$processingModelName/coloring/coloringlist.txt* >> $log 2>&1
  else
    echo "File(s) coloring*.smd exist -- skipping generation of plate coloring metadata" >> $log 2>&1
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

doGzipDir() {
  dir=$1
  if test "x$dir" = x; then
    echo "Cannot gzip files in missing/blank directory." >> $log 2>&1
    exit 1
  fi
  if test ! -d $dir; then
    echo "Cannot gzip files in $dir: not a directory." >> $log 2>&1
    exit 1
  fi
  for file in $dir/*; do
    if test -f $file; then
      if test `file $file 2>&1 | grep -ic gzip` -eq 0; then
        gzip -cf $file > $file.gz  2>> $log
        if test $? -ne 0; then
          echo "Problem gzipping file $file" >> $log 2>&1
          exit 1
        fi
        rm -f $file 2>> $log
      fi
    fi
  done
}

doGzipDirIfNecessary() {
  dir=$1
  if test "x$dir" = x; then
    echo "Cannot gzip files in missing/blank directory." >> $log 2>&1
    exit 1
  fi
  if test -d $dir; then
    doGzipDir $dir
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

echo "Starting rawdata2processed-ryugu.sh script (log file: $log)"
mkdir -p $destTop                                                                                 
                        
makeLogDir

echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "Begin `date`" >> $log 2>&1

if [ $processingModelName = "shared" ]
then
   # if the model name is "shared" then we only copy over the new shared data
   createDirIfNecessary $destTop/shared
   createDirIfNecessary $destTop/shared/onc
   createDirIfNecessary $destTop/shared/onc/images
   createDirIfNecessary $destTop/shared/onc/infofiles
   createDirIfNecessary $destTop/shared/onc/gallery
   createDirIfNecessary $destTop/shared/"history"
   createDirIfNecessary $destTop/shared/tir
   createDirIfNecessary $destTop/shared/tir/images
   createDirIfNecessary $destTop/shared/lidar
   createDirIfNecessary $destTop/shared/lidar/browse


#*** don't uncomment old code ***
#   for dateDir in `ls $srcTop/shared/onc`
#   do
#     echo Rsyncing image files from $srcTop/shared/onc/$dateDir... >> $log 2>&1
#     doRsyncDir $srcTop/shared/onc/images $destTop/shared/onc/images

     # remove extraneous html files
#     rm -f $destTop/shared/onc/images/index.html*
     
     # fix any bad permissions
     #$scriptDir/data-permissions.pl $destTop/shared/onc/images

#   done
#*** end of old code ***

   # old code above replaced with this, copies ONC images from rawdata to processed 
   doRsyncDir $srcTop/shared/onc/images $destTop/shared/onc/images

   # remove extraneous html files
   rm -f $destTop/shared/onc/images/index.html*

   # edits metakernel so that the correct path value is write in the metakernel file
   editKernel 
 
   # generates info files for ONC-Ti
   # takes 6 parameters, [source-dir] [dest-dir] [most-updated-kernel] [body-name-uppercase] [camera-name-informal] [camera-name-formal]
   generateInfoFiles $srcTop/shared $destTop/shared $latestKernel RYUGU onc "HAYABUSA2_ONC-T"

   # generates info files for ONC-W1
   # takes 6 parameters, [source-dir] [dest-dir] [most-updated-kernel] [body-name-uppercase] [camera-name-informal] [camera-name-formal]
   generateInfoFiles $srcTop/shared $destTop/shared $latestKernel RYUGU onc "HAYABUSA2_ONC-W1"

   # becuase there is the onc-w1 and onc-t camera, the files all need to be merged again into one imagelist-info.txt and one imagelist-fullpath-info.txt
   # *** CANNOT HANDLE THE FUTURE W2 IMAGES, the code below must be changed to merge those files in as well ***
   cat $destTop/shared/onc/"HAYABUSA2_ONC-W1-info.txt" >> $destTop/shared/onc/"HAYABUSA2_ONC-T-info.txt"
   mv $destTop/shared/onc/"HAYABUSA2_ONC-T-info.txt" $destTop/shared/onc/"imagelist-info.txt"

   cat $destTop/shared/onc/"HAYABUSA2_ONC-W1-fullpath-info.txt" >> $destTop/shared/onc/"HAYABUSA2_ONC-T-fullpath-info.txt"
   mv $destTop/shared/onc/"HAYABUSA2_ONC-T-fullpath-info.txt" $destTop/shared/onc/"imagelist-fullpath-info.txt"

   # combines the generated infofiles for the 2 camera types into one infofiles directory
   mv $destTop/shared/onc/"HAYABUSA2_ONC-W1-infofiles"/*.INFO $destTop/shared/onc/infofiles
   mv $destTop/shared/onc/"HAYABUSA2_ONC-T-infofiles"/*.INFO $destTop/shared/onc/infofiles

   # generates gallery for ONC
   # takes 5 parameters [*.fileExtension] [image-source] [gallery-destination] [body-name-lowercase] [path-of-imagelist-info.txt]
   generateGallery "*.fit" $srcTop/shared/onc/images $destTop/shared/onc/gallery $bodyName $destTop/shared/onc/imagelist-info.txt
   
   # copy over TIR images from rawdata to processed
   doRsyncDir $srcTop/shared/tir/images $destTop/shared/tir/images

   # generates info files for TIRi
   # takes 6 parameters, [source-dir] [dest-dir] [most-updated-kernel] [body-name-uppercase] [camera-name-informal] [camera-name-formal]
   generateInfoFiles $srcTop/shared $destTop/shared $latestKernel RYUGU tir "HAYABUSA2_TIR-S"

   # generates gallery for TIRi
   # takes 5 parameters [*.fileExtension] [image-source] [gallery-destination] [body-name-lowercase] [path-of-imagelist-info.txt]
   generateGallery "*.fit" $srcTop/shared/tir/images $destTop/shared/tir/gallery $bodyName $destTop/shared/tir/imagelist-info.txt
   
    # copy over LIDAR data from rawdata to processed
   doRsyncDir $srcTop/shared/lidar/browse $destTop/shared/lidar/browse
   
   # generates fileList for LIDAR
   # takes 1 parameters [lidar-sourcefile]
   processLidarBrowse $destTop/shared/lidar/browse/

   # deletes existing timeHistory files and generates a new one
   generateTimeHistory
   
   # removed extra files that were generated and the time history csv (not the final timeHistory.bth product)
   rm -rf $destTop/shared/onc/HAYABUSA2*
   rm -rf $destTop/shared/onc/*Files.txt
   rm -rf $destTop/shared/tir/*Files.txt
   rm -rf $destTop/shared/"history"/$spacecraftName*
   
   echo Correcting permissions >> $log 2>&1
   $scriptDir/data-permissions.pl $destTop/shared

else
   createDirIfNecessary $destTop/$processingModelName/shape
   #createDirIfNecessary $destTop/$processingModelName/onc
   #createDirIfNecessary $destTop/$processingModelName/onc/images
   #createDirIfNecessary $destTop/$processingModelName/onc/gallery
   #createDirIfNecessary $destTop/$processingModelName/tir
   #createDirIfNecessary $destTop/$processingModelName/tir/images
   #createDirIfNecessary $destTop/$processingModelName/tir/gallery

   # Process the shape models.
   doRsyncDirIfNecessary $srcTop/$rawdataModelName/shape/ $destTop/$processingModelName/shape/
   doGzipDirIfNecessary $destTop/$processingModelName/shape

   # processes ONC sumfiles *** CANNOT PROCESS TIR SUMFILES ***
   if [ -d "$srcTop/$rawdataModelName/onc" ]
   then
     echo Beginning sumfile processing
     createDirIfNecessary $destTop/$processingModelName/onc     

     # generates imagelist-sum.txt and imagelist-fullpath.txt
     processMakeSumfiles $srcTop/$rawdataModelName/onc/ 
     
     # copies over onc directory
     doRsync $srcTop/$rawdataModelName/onc/ $destTop/$processingModelName/onc/

     # *** process imager, not actually sure what this does *** james should know and I just realized that it proabbly has no effect since the rsync is already done
     processImager
     echo Finished sumfile processing
   fi

   # process coloring data and generates the metadata needed to read the colroing data.
   if [ -d "$srcTop/$rawdataModelName/coloring" ] 
   then
      echo discovering plate colorings
      createDirIfNecessary $destTop/$processingModelName/coloring
      doRsync $srcTop/$rawdataModelName/coloring/ $destTop/$processingModelName/coloring/
     
      # gzips the coloring files
      doGzipDir $destTop/$processingModelName/coloring/*.fits

      # runs James' java tool, DiscoverPlateColorings, that class ues an intermediate script, ls-pc.sh which is located in /project/sbmt2/sbmt/scripts
      discoverPlateColorings
      echo finished processing plate colorings
   fi

   # fix any bad permissions
   echo Correcting permissions >> $log 2>&1
   $scriptDir/data-permissions.pl $destTop/$processingModelName

fi

echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
echo "Finished rawdata2processed-ryugu.sh script"

