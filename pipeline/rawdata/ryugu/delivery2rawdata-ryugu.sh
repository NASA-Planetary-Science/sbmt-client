#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:      Russell Turner and Alex Welsh
# Redmine:        sbmt1dev issue #1304
# Description:    Test version of script for transforming delivered JAXA data into
#                 rawdata format
#-------------------------------------------------------------------------------

# Usage
if [ "$#" -lt 2 ]
then
  echo "Model data usage:  delivered2rawdata-ryugu.sh <delivered-model-name> <delivered-version> [ <processed-model-name> <processing-version> ]"
  echo "Shared data usage: delivered2rawdata-ryugu.sh shared"
  exit 1
fi

# Command line parameters
deliveredModelName=$1
deliveredVersion=$2
if [ "$#" -ge 3 ]
then
  processingModelName=$3
else
  processingModelName=$deliveredModelName
fi
if [ "$#" -ge 4 ]
then
  processingVersion=$4
else
  processingVersion=$deliveredVersion
fi

bodyName="ryugu"

pipelineTop="/project/sbmtpipeline"
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
  #latestDateDir=``ls | sort -r | head -1`/kernels/mk`
  #latestKernel=`ls $latestDateDir/hyb2_approach_v*.tm | sort -r | head -1`
fi

echo "Delivered version: " $deliveredVersion
echo "Delivered model name: " $deliveredModelName
echo "Processing version: " $processingVersion
echo "Processing model name: " $processingModelName



rawTop="$pipelineTop/rawdata"
processedTop="$pipelineTop/processed"

scriptDir="/project/sbmt2/sbmt/scripts"
importCmd="$scriptDir/import.sh"
rsyncCmd='rsync -rlptgDH --copy-links'

srcTop="$deliveriesTop/$bodyName/$deliveredVersion"
destTop="$rawTop/$bodyName/$processingVersion"

releaseDir="$destTop"

logDir="$destTop/logs"
log="$logDir/delivery2rawdata.log"

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
    src=`echo $src | sed -e 's:/*$:/:'`
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

# Runs in a sub-shell. Indicates success by touching a marker file.
checkoutCodeIfNecessary() (
  markerFile="$srcTop/git-succeeded.txt"
  if test ! -f $markerFile;  then
    cd $srcTop >> $log 2>&1
    chgrp sbmtsw .
    chmod 2775 .
    if test $? -ne 0; then
      echo "Unable to checkout code in directory $srcTop" >> $log 2>&1
      exit 1
    fi
    echo "In directory $srcTop" >> $log 2>&1
    echo "git clone http://hardin:8080/scm/git/vtk/saavtk --branch saavtk1dev" >> $log 2>&1
    git clone http://hardin:8080/scm/git/vtk/saavtk --branch saavtk1dev >> $log 2>&1
    if test $? -ne 0; then
      echo "Unable to git clone saavtk" >> $log 2>&1
      exit 1
    fi
    echo "git clone http://hardin:8080/scm/git/sbmt --branch sbmt1dev" >> $log 2>&1
    git clone http://hardin:8080/scm/git/sbmt --branch sbmt1dev >> $log 2>&1
    if test $? -ne 0; then
      echo "Unable to git clone sbmt" >> $log 2>&1
      exit 1
    fi
    touch $markerFile
  else
    echo "Marker file $markerFile exists already; skipping git clone commands" >> $log 2>&1
  fi
)

# Runs in a sub-shell. Indicates success by touching a marker file.
buildCodeIfNecessary() (
  markerFile="$srcTop/make-release-succeeded.txt"
  if test ! -f $markerFile;  then
    cd $srcTop/sbmt >> $log 2>&1
    chgrp sbmtsw .
    chmod 2775 .
    if test $? -ne 0; then
      echo "Unable to build code in directory $srcTop/sbmt" >> $log 2>&1
      exit 1
    fi

    # Before building, need to set the released mission.
    $SBMTROOT/misc/scripts/set-released-mission.sh APL_INTERNAL
    if test $? -ne 0; then
      echo "Setting the released mission failed in directory $srcTop/sbmt" >> $log 2>&1
      exit 1
    fi

    # Capture this step in its own log file.
    echo "Building code in $srcTop/sbmt; see log $srcTop/sbmt/make-release.txt" >> $log 2>&1
    make release > make-release.txt 2>&1
    if test $? -ne 0; then
      echo "Make release failed in directory $srcTop/sbmt" >> $log 2>&1
      exit 1
    fi

    touch $markerFile
  else
    echo "Marker file $markerFile exists already; skipping build step" >> $log 2>&1
  fi
)


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

echo "Starting deliveries2rawdata-ryugu.sh script (log file: $log)"

makeLogDir

echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "Begin `date`" >> $log 2>&1

# Check out and build code.
checkoutCodeIfNecessary
if test $? -ne 0; then
  exit 1
fi
buildCodeIfNecessary
if test $? -ne 0; then
  exit 1
fi


# Import everything from the delivery directory

if [ $processingModelName = "shared" ]
then
   # if the model name is "shared" then we only copy over the new shared data
   createDirIfNecessary $destTop/shared
   createDirIfNecessary $destTop/shared/onc
   createDirIfNecessary $destTop/shared/spice
   createDirIfNecessary $destTop/shared/onc/images
   createDirIfNecessary $destTop/shared/tir
   createDirIfNecessary $destTop/shared/lidar

   echo copying kernels
   doRsyncDir $srcTop/spice/ $destTop/shared/spice/
   echo done with job
   
   # *** All of these images are copied from their respective l2a (L2a) directories. ***

   # copies over .fit files from the Box-C directory in deliveries-hyb2
   #for dateDir in `ls $srcTop/onc/Box-C`
   #do
   #  echo Rsyncing image files from ryugu/latest/onc/Box-C/$dateDir... >> $log 2>&1
   #  for imageName in `ls $srcTop/onc/Box-C/$dateDir/L2a/*.fit` # doRsync cannot handle *.fit so a seocnd for loop is needed to pass the specific name
   #  do
   #    doRsync $imageName $destTop/shared/onc/images
   #  done
   #done

   # copies over .fit files from the Box-A directory in deliveries-hyb2
   #for dateDir in `ls $srcTop/onc/Box-A`
   #do
   #  echo Rsyncing image files from ryugu/latest/onc/Box-A/$dateDir... >> $log 2>&1
   #  for imageName in `ls $srcTop/onc/Box-A/$dateDir/L2a/*.fit` # doRsync cannot handle *.fit so a seocnd for loop is needed to pass the specific name
   #  do
   #    doRsync $imageName $destTop/shared/onc/images
   #  done
   #done

   # copies over .fit files from the MidAlt directory in deliveries-hyb2
   #for dateDir in `ls $srcTop/onc/MidAlt`
   #do
   #  echo Rsyncing image files from ryugu/latest/onc/MidAlt/$dateDir... >> $log 2>&1
   #  for imageName in `ls $srcTop/onc/MidAlt/$dateDir/L2a/*.fit` # doRsync cannot handle *.fit so a seocnd for loop is needed to pass the specific name
   #  do
   #    doRsync $imageName $destTop/shared/onc/images
   #  done
   #done

   # copies over .fit files from the Gravity directory in deliveries-hyb2
   #for dateDir in `ls $srcTop/onc/Gravity`
   #do
   #  echo Rsyncing image files from ryugu/latest/onc/Gravity/$dateDir... >> $log 2>&1
   #  for imageName in `ls $srcTop/onc/Gravity/$dateDir/L2a/*.fit` # doRsync cannot handle *.fit so a seocnd for loop is needed to pass the specific name
   #  do
   #    doRsync $imageName $destTop/shared/onc/images
   #  done
   #done

   # copies over .fit files from the FromONC_CoI_Server directory in deliveries-hyb2
   for dateDir in `ls $srcTop/onc/FromONC_CoI_Server`
   do
     echo Rsyncing image files from ryugu/latest/onc/FromONC_CoI_Server/$dateDir... >> $log 2>&1
     for imageName in `ls $srcTop/onc/FromONC_CoI_Server/$dateDir/*.fit` # doRsync cannot handle *.fit so a seocnd for loop is needed to pass the specific name
     do
       #doRsyncDir $srcTop/onc/FromONC_CoI_Server/$dateDir/ $destTop/shared/onc/$dateDir #*** don't uncomment old code ***
       doRsync $imageName $destTop/shared/onc/images
     done
   done

   # copies over .fit files from the Approach directory in deliveries-hyb2
   #for dateDir in `ls $srcTop/onc/Approach`
   #do
   #  if [ "$dateDir" -gt  20180615 ] 
   #  then
   #	echo Rsyncing image files from ryugu/latest/onc/Approach/$dateDir/L2a
   #     for imageName in `ls $srcTop/onc/Approach/$dateDir/L2a/*.fit` # doRsync cannot handle *.fit so a seocnd for loop is needed to pass the specific name
   #     do
   #       doRsync $imageName $destTop/shared/onc/images
   #     done
   #  fi
   #done

   # copies over .fit files from the tir/l2a directory in deliveries-hyb2
   for tirDir in `ls -d $srcTop/tir/l2a/*/`
   do
     echo $tirDir
     for imageName in `ls $tirDir/*.fit` # doRsync cannot handle *.fit so a seocnd for loop is needed to pass the specific name
     do
       doRsync $imageName $destTop/shared/tir/images/
     done
   done
   
   # copies over .csv files from the lidar/ directory in deliveries-hyb2.  They match the following file patterns: hyb2_ldr_l2_aocsm_topo_ts* hyb2_ldr_l2_hk_topo_ts*
   for lidarDir in `ls -d $srcTop/lidar/`
   do
     echo $lidarDir
     for lidarName in `ls $lidarDir/hyb2_ldr_l2_aocsm_topo_ts*` # doRsync cannot handle *.fit so a second for loop is needed to pass the specific name
     do
       doRsync $lidarName $destTop/shared/lidar/browse/
     done
     for lidarName in `ls $lidarDir/hyb2_ldr_l2_hk_topo_ts*` # doRsync cannot handle *.fit so a second for loop is needed to pass the specific name
     do
       doRsync $lidarName $destTop/shared/lidar/browse/
     done
   done

   echo fixing permissions
   scriptDir/data-permissions.pl $destTop/shared
   
else
	
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

   # copy the shape model
   doRsyncDir $srcTop/$deliveredModelName/shape $destTop/$processingModelName/shape
   
   # Copy coloring files
	doRsyncDirIfNecessary $srcTop/$deliveredModelName/coloring $destTop/$processingModelName/coloring

   if [ -d "$srcTop/$deliveredModelName/onc" ] 
   then
     createDirIfNecessary $destTop/$processingModelName/onc

     # copy the onc imaging files
     doRsyncDir $srcTop/$deliveredModelName/onc $destTop/$processingModelName/onc
   fi

   if [ -d "$srcTop/$deliveredModelName/tir" ]
   then
     createDirIfNecessary $destTop/$processingModelName/tir

     # copy the tir imaging files
     doRsyncDir $srcTop/$deliveredModelName/tir $destTop/$processingModelName/tir
   fi

   if [ -d "$srcTop/$deliveredModelName/nirs3" ]
   then
     createDirIfNecessary $destTop/$processingModelName/nirs3

     # copy the nirs3 imaging files
     doRsyncDir $srcTop/$deliveredModelName/nirs3 $destTop/$processingModelName/nirs3
   fi

   if [ -d "$srcTop/$deliveredModelName/coloring" ]
   then
     createDirIfNecessary $destTop/$processingModelName/coloring

     # copy the coloring files
     doRsyncDir $srcTop/$deliveredModelName/coloring $destTop/$processingModelName/coloring
   fi
   
    if [ -d "$srcTop/$deliveredModelName/lidar" ]
   then
     createDirIfNecessary $destTop/$processingModelName/lidar

     # copy the coloring files
     doRsyncDir $srcTop/$deliveredModelName/lidar $destTop/$processingModelName/lidar
   fi
   
   echo fixing permissions
   scriptDir/data-permissions.pl $destTop/$processingModelName
fi


echo removing unused files
rm -rf $destTop/shared/onc/images/*.tgz
rm -rf $destTop/shared/onc/images/*.d
rm -rf $destTop/shared/onc/images/index.html*

echo "End `date`" >> $log 2>&1
echo "--------------------------------------------------------------------------------" >> $log 2>&1
echo "" >> $log 2>&1
echo "Finished deliveries2rawdata-ryugu.sh script"
