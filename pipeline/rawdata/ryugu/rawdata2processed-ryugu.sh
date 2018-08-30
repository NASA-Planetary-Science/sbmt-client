#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:   Russell Turner and Alex Welsh
# Redmine:     sbmt1dev issue #1304
# Description: Test version of script for transforming raw JAXA data into
#              processed format
#-------------------------------------------------------------------------------

# usage
if [ "$#" -eq 0 ]
then
  echo "Model data usage:  rawdata2processed-ryugu.sh <model-name> <processed-date> [ <processed-model-name> <processed-date> ]"
  echo "Shared data usage: rawdata2processed-ryugu.sh shared"
  exit 1
fi

pipelineTop="/project/sbmtpipeline"
bodyName="ryugu"
bodyNameCaps="RYUGU"
spacecraftName="HAYABUSA2"

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

if [ $processingModelName = "shared" ]
then                                                                                              
   processingVersion="latest"
fi                                                                                                
                                                                                                  
echo "Body name:$bodyName"
echo "Processing model name:$processingModelName"
echo "Processing version:$processingVersion"

legacyTop="/project/sbmt2/sbmt/nearsdc/data"
deliveriesTop="$pipelineTop/deliveries"
rawdataTop="$pipelineTop/rawdata"
processedTop="$pipelineTop/processed"
deployedTop="/project/sbmt2/sbmt/data/bodies"

scriptDir="/project/sbmt2/sbmt/scripts"
timeHistoryDir="$pipelineTop/timeHistory"
importCmd="$scriptDir/import.sh"
rsyncCmd='rsync -rlptgDH --copy-links'
briefPgrm="/project/nearsdc/software/spice/cspice/exe/brief"
javaCmd="/project/nearsdc/software/java/x86_64/latest/bin/java"

srcTop="$rawdataTop/$bodyName/$processingVersion"
destTop="$processedTop/$bodyName/$processingVersion"

releaseDir="$srcTop/$processingModelName"
export SBMTROOT="$releaseDir/sbmt"
export SAAVTKROOT="$releaseDir/saavtk"

# figures out what the latest kernel is for use in many of the processing methods
if [ $processingModelName = "shared" ]
then
   latestMKDir=$srcTop/shared/spice/kernels/mk
   #`ls $srcTop/shared/spice | sort -r | head -1`/kernels/mk
   latestKernel=`ls $latestMKDir/hyb2_analysis_v*.tm | sort -r | head -1`
   echo $latestMKDir
   echo $latestKernel
fi 

log="$srcTop/logs/rawdata2processed-model.log"

# echo "Source Top: " $srcTop                                                                       
# echo "Dest Top: " $destTop                                                                        
# echo "Log file: " $log

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
  echo "" >> $log 2>&1
#  echo "--------------------------------------------------------------------------------" >> $log 2>&1
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
  destImageList="$destImager/imagelist-sum.txt"
  destImageListFullPath="$destImager/imagelist-fullpath-sum.txt"
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

# Copies over the files from rawdata and amkes fileList.txt file containing body relative path followed by start and stop time of track in ET
processLidarBrowse() (
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

)

# generates the imagelist-fullpath-sum.txt and imagelist-sum.txt files if SUM files are delivered.
processMakeSumfiles() (
  imageDir=$1
  instrument=`basename $imageDir`

  # write imagelist-fullpath-sum.txt
  if test -e $imageDir/imagelist-fullpath-sum.txt; then
     rm $imageDir/imagelist-fullpath-sum.txt
  fi
  fullpath=/$bodyName/$processingModelName/$instrument/images/
  awk '{print $8}' $imageDir/make_sumfiles.in | sed -e "s:^:$fullpath:" > $imageDir/imagelist-fullpath-sum.txt


  # write imagelist-sum.txt
  if test -e $imageDir/imagelist-sum.txt; then
     rm $imageDir/imagelist-sum.txt
  fi
  fileNames=`awk '{print $8" "}' $imageDir/make_sumfiles.in`
  for line in $fileNames
  do
     # extracts the time from the file name
     fileTime=`echo $line | sed -e 's:[^_]*_[^_]*_::' | sed -e 's:\(.*[0-9][0-9][0-9][0-9]\).*:\1:' |
     sed -e 's:\(..\)$:\:\1:' | sed -e 's:\(..\:\):\:\1:' | sed -e 's:_:T:' | sed -e 's:\(..T\):-\1:' | sed -e 's:\(..-\):-\1:'`
     echo "$line $fileTime" >> $imageDir/imagelist-sum.txt
  done
)

# edits the PATH SYMBOL in all the kernels in shared/spice/kernels/mk directory with the path to the metakernel
editKernel() (
  echo Editing metakernel to have correct path value 
  for metaKernel in $latestMKDir/*.tm
  do
    echo Processing $metaKernel 
    kernelsDir=`dirname $latestMKDir` 
    sed -i "s#\(PATH_VALUES *= *\).*#\1( '$kernelsDir' ) #" $metaKernel
  done
  echo Finished editing metakernels
)

# called the generateInfoFiles.sh script in /project/sbmt2/sbmt/scripts and generates the INFO files, that script also uses creat_info_files, located in the scripts dir as well.
generateInfoFiles() (
  CMD="$scriptDir/generateInfoFiles.sh $1 $2 $3 $4 $5 $6"
  $CMD   
)

# generates the CVS time history (later removed by this script) and the final timeHistory.bth
# the c++ code that generates the csv is located in projects/sbmtpipeline/timeHistory. there are 3 scripts, getFov.c getSpacecraftState.c and getTargetState.c 
# had to be modified specifically for Ryugu (IAU_RYUGU_FIXED). If those need to be edited, do so and run comple.c (in the timeHistory directory) and a new c++ program will be generated
# the ConvertCSVToBinary.class is located in /project/sbmt2/sbmt/scripts 
# Lillian can have more information
generateTimeHistory() (
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
)

# makes the gallery using 2 scripts, fits2thumbnails.py and make_gallery_webpage.py, both located in /project/sbmt2/sbmt/scripts
generateGallery() (
  echo Start gallery generation
  F2T="python $scriptDir/fits2thumbnails.py $2 $3"
  makeWebpage="python $scriptDir/make_gallery_webpage.py $1 $2 $3 $4 $5"
  $F2T
  $makeWebpage
  echo End gallery generation
)

# runs ls-pc.sh and DiscoverPlateColorings.sh which is linked to a java tool. This uses code in a release that must be made in the directory containing the model. Ask James for guidance
# essentially a release must be made in the model and this code uses that to keep control of changes over time.
discoverPlateColorings() (
  $scriptDir/"ls"-pc.sh $destTop/$processingModelName/coloring
  modelName=`echo $processingModelName | sed 's:\([^v20]*\):\U\1:'` 
  $releaseDir/sbmt/bin/DiscoverPlateColorings.sh $destTop/$processingModelName/coloring $bodyName/$processingModelName/coloring "$modelName/162173 Ryugu"
  #$makeColorings
)

#-------------------------------------------------------------------------------
# MAIN SCRIPT STARTS HERE.
#-------------------------------------------------------------------------------

echo "Starting rawdata2processed-ryugu.sh script (log file: $log)"
mkdir -p $destTop                                                                                 
                                                                                                  
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "Begin `date`" >> $log 2>&1

# echo "Source Top: " $srcTop/$processingModelName
# echo "Dest Top: " $destTop/$processingModelName

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
   
   # generates fileList for LIDAR
   # takes 1 parameters [lidar-sourcefile]
   processLidarBrowse $srcTop/shared/lidar/browse/

   # deletes existing timeHistory files and generates a new one
   generateTimeHistory
   
   # removed extra files that were generated and the time history csv (not the final timeHistory.bth product)
   rm -rf $destTop/shared/onc/HAYABUSA2*
   rm -rf $destTop/shared/onc/*Files.txt
   rm -rf $destTop/shared/tir/*Files.txt
   rm -rf $destTop/shared/"history"/$spacecraftName*

else
   createDirIfNecessary $destTop/$processingModelName/shape
   #createDirIfNecessary $destTop/$processingModelName/onc
   #createDirIfNecessary $destTop/$processingModelName/onc/images
   #createDirIfNecessary $destTop/$processingModelName/onc/gallery
   #createDirIfNecessary $destTop/$processingModelName/tir
   #createDirIfNecessary $destTop/$processingModelName/tir/images
   #createDirIfNecessary $destTop/$processingModelName/tir/gallery

   # copies over the shape models and gzips them
   doRsync $srcTop/$rawdataModelName/shape/ $destTop/$processingModelName/shape/
   gzip -f $destTop/$processingModelName/shape/*.obj

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
     
      # gzips the colroing files
      gzip -f $destTop/$processingModelName/coloring/*.fits

      # runs James' java tool, DiscoverPlateColorings, that class ues an intermediate script, ls-pc.sh which is located in /project/sbmt2/sbmt/scripts
      discoverPlateColorings
      echo finished processing plate colorings
   fi

   # fix any bad permissions
   $scriptDir/data-permissions.pl $destTop/$processingModelName

fi

# fix any bad permissions
$scriptDir/data-permissions.pl $destTop/$processingModelName                                      

echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
echo "Finished rawdata2processed-ryugu.sh script"

