#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:      James Peachey, based on commonFuncs.sh by Russell Turner,
#                 Alex Welsh and Josh Steele.
# Description:    Utilities to support processing data in context of
#                 importing models, images, spectra etc. into SBMT or
#                 similar clients.
#-------------------------------------------------------------------------------

# Check the exit status of a previous command, which is passed as the first
# argument. If it's non-0, print an optional context messsage if present,
# and then call exit from within the invoking shell, passing along the
# provided status.
#
# Because this exits and does not run in a sub-shell, use cautiously.
check() {
  if test "$1" = ""; then
    echo "check: first argument is blank" >&2
    if test $# -gt 1; then
      echo "$*" >&2
    fi
    exit 1
  fi

  # This strange-looking check is necessary for bullet-proofing in case $1
  # doesn't contain an integer. A simple if test... does not behave reliably.
  # To make matters worse, exit won't actually terminate the invoking shell
  # in such a case either. Bottom line: if you are debugging and you get an
  # error on the next line, check the calling code, which must not be
  # passing an integer for argument 1.
  ( exit $1 )
  
  # If $1 did contain an integer, the exit status will be that integer.
  # Otherwise, status will be some other integer. In any case, status
  # for sure will now be an integer, so it will behave itself in the test. 
  status=$?
  if test $status -ne 0; then
    if test $# -gt 1; then
      echo "status=$*" >&2
    fi
    exit $status
  fi
}

# Get the absolute path of the directory for the path passed as an argument.
# If the argument identifies an existing directory, this function returns the
# path to that directory. If the argument identifies a file, this function
# returns the parent directory of that file. If the argument does not identify
# either a file or a directory, an error is thrown and the invoking shell will
# exit.
#
# An optional second argument is the error message that will be shown before
# exit is called.
#
# Calling this function does not change the current path in the invoking shell,
# but this function does use "cd" in a sub-shell to go to the directory whose
# path it returns.   
getDirName() {
  if test "x$1" = x; then
    echo "$2 getDirName: directory name is blank." >&2
    exit 1
  fi

  msg=$2
  
  if test -f "$1"; then
    result="$(cd "$(dirname "$1")"; check $? $msg getDirName: cannot cd to parent of $1; pwd -P)"
    check $? "$msg getDirName: cannot determine parent of $1" 
  else
    result="$(cd $1; check $? $msg getDirName: cannot cd to $1; pwd -P)"
    check $? "$msg getDirName: cannot determine directory name of $1" 
  fi

  echo $result
}

# Guess the parent of a "raw data" path (supplied as an argument).
# The first guess is that the supplied path contains a segment called "rawdata".
# If this segment is found, the parent is assumed to be the part of the path
# to the left of the "rawdata" segment. If the path contains more than one
# segment named "rawdata", the right-most one will be used.
#
# If the supplied path does not include the segment "rawdata", 
#
# This function uses getDirName to rationalize paths, so the supplied argument
# must actually exist in the file system.  
guessRawDataParentDir() {
  if test "$1" = ""; then
    echo "guessRawDataParentDir: no directory supplied as an argument" >&2
    exit 1
  fi

  dir=$(getDirName "$1")

  # Extract everything to the right of right-most rawdata/.
  suffix=`echo $dir | sed -n 's:.*/rawdata/::p'`

  if test "$suffix" != ""; then
    result=`echo $dir | sed "s:/rawdata/$suffix$::"`
  elif test `echo $dir | grep -c '/rawdata$'` -gt 0; then
    result=`echo $dir | sed "s:/rawdata$::"`
  else
    echo "guessRawDataParentDir: can't guess rawdata location from $dir" >&2
    exit 1
  fi
  
  echo $result
}

# Create a directory if it doesn't already exist.
createDir() {
  (
    dir=$1
    if test "x$dir" = x; then
      echo "createDir: missing/blank directory argument." >&2
      exit 1
    fi

    if test ! -d "$dir"; then
      echo "mkdir -p $dir"
      mkdir -p "$dir"
      if test $? -ne 0; then
        echo "createDir: unable to create directory $dir." >&2
        exit 1
      fi
    fi
  )
  if test $? -ne 0; then exit 1; fi
}

# Create the parent of a file/directory if it doesn't already exist.
createParentDir() {
  (
    dir=$1
    if test "x$dir" = x; then
      echo "createParentDir: missing/blank directory argument." >&2
      exit 1
    fi

    parentDir=$(dirname "$dir")"/.."
    if test ! -d "$parentDir"; then
      echo "mkdir -p $parentDir"
      mkdir -p "$parentDir"
      if test $? -ne 0; then
        echo "createParentDir: unable to create directory $parentDir." >&2
        exit 1
      fi
    fi
  )
  if test $? -ne 0; then exit 1; fi
}

# Perform an rsync from the source to the destination.
doRsync() {
  (
    src=$1
    dest=$2

    # Make sure source directories end in a slash.
    if test -d $src; then
      src=`echo $src | sed -e 's:/*$:/:'`
    fi

    rsyncCmd="rsync -rlptgDH --copy-links"
    echo nice time $rsyncCmd $src $dest
    nice time $rsyncCmd $src $dest
    if test $? -ne 0; then
      echo "Failed to rsync $src $dest" >&2
      exit 1
    fi

    echo ""
  )
  if test $? -ne 0; then exit 1; fi
}

# Perform an rsync from the source to the destination. Both must be directories.
doRsyncDir() {
  (
    src=$1
    dest=$2
    if test ! -e $src; then
      echo "Source $src does not exist" >&2
      exit 1
    fi
    if test ! -d $src; then
      echo "Source $src is unexpectedly not a directory." >&2
      exit 1
    fi
    if test -e $dest -a ! -d $dest; then
      echo "Destination $dest exists but is unexpectedly not a directory." >&2
      exit 1
    fi
    createDir $dest
    doRsync $src $dest
  )
  if test $? -ne 0; then exit 1; fi
}

# Perform an rsync from a source directory to the destination, but only if the
# source directory exists.
doRsyncOptionalDir() {
  (
    src=$1
    dest=$2
    # Check only existence here. doRsyncDir will take care of
    # reporting error if src is not a directory.
    if test -e "$src"; then
      doRsyncDir "$src" "$dest"
    fi
  )
  if test $? -ne 0; then exit 1; fi
}

# Set srcTop and destTop to point to source and destination top location.
# Then call this, passing src and dest relative to these "top" directories.
copyFile() {
  (
    if test "x$1" = x; then
      echo "copyFile: source file name is not set" >&2
    fi
    src=$1
    if test "x$2" = x; then
      dest=$src
    else
      dest=$2
    fi
 
    if test "x$srcTop" = x; then
      echo "copyFile: Variable srcTop is not set" >&2
      exit 1
    fi
    if test "x$destTop" = x; then
      echo "copyFile: Variable destTop is not set" >&2
      exit 1
    fi
    doRsync "$srcTop/$src" "$destTop/$dest"
  )
  if test $? -ne 0; then exit 1; fi
}

# Copy a directory. Throws an error and quits if source is missing.
# Set srcTop and destTop to point to source and destination top location.
# Then call this, passing src and dest relative to these "top" directories.
copyDir() {
  (
    if test "x$1" = x; then
      echo "copyDir: source directory name is not set" >&2
    fi
    src=$1
    if test "x$2" = x; then
      dest=$src
    else
      dest=$2
    fi
  
    if test "x$srcTop" = x; then
      echo "copyDir: Variable srcTop is not set" >&2
      exit 1
    fi
    if test "x$destTop" = x; then
      echo "copyDir: Variable destTop is not set" >&2
      exit 1
    fi
    doRsyncDir "$srcTop/$src" "$destTop/$dest"
  )
  if test $? -ne 0; then exit 1; fi
}

# Copy a directory, but only if the source directory exists. No error, no op if it's missing.
# Set srcTop and destTop to point to source and destination top location.
# Then call this, passing src and dest relative to these "top" directories.
copyOptionalDir() {
  (
    if test "x$1" = x; then
      echo "copyOptionalDir: source directory name is not set" >&2
    fi
    src=$1
    if test "x$2" = x; then
      dest=$src
    else
      dest=$2
    fi
  
    if test "x$srcTop" = x; then
      echo "copyOptionalDir: Variable srcTop is not set" >&2
      exit 1
    fi
    if test "x$destTop" = x; then
      echo "copyOptionalDir: Variable destTop is not set" >&2
      exit 1
    fi
    doRsyncOptionalDir "$srcTop/$src" "$destTop/$dest"
  )
  if test $? -ne 0; then exit 1; fi
}

moveDirectory() {
  (
    src=$1
    dest=$2
    if test "x$src" = x -o "x$dest" = x; then
      echo "Missing argument(s) to moveDirectory" >&2
      exit 1
    fi
    if test -d $src; then
      if test -e $dest; then
        rm -rf $dest-bak
        mv -f $dest $dest-bak
        if test $? -ne 0; then
          echo "Unable to back up $dest; not moving directory $src" >&2
          exit 1
        fi
      fi

      destParent=`echo $dest | sed 's:/[^/][^/]*/*$::'`
      createDir $destParent
      if test $? -ne 0; then exit 1; fi

      echo "mv $src $dest"
      mv -f $src $dest
      if test $? -ne 0; then exit 1; fi

      # Prune an orphaned parent directory, but ignore failures.
      srcParent=`echo $src | sed 's:/[^/][^/]*/*$::'`
      rmdir $srcParent >> /dev/null 2>&1
      exit 0
    else
      echo "Not moving/renaming $src (is not a directory)"
    fi
  )
  if test $? -ne 0; then exit 1; fi
}

moveFile() {
  (
    src=$1
    dest=$2
    if test "x$src" = x -o "x$dest" = x; then
      echo "Missing argument(s) to moveDirectory" >&2
      exit 1
    fi
    if test -f $src; then
      if test -e $dest; then
        rm -rf $dest-bak
        mv $dest $dest-bak
        if test $? -ne 0; then
          echo "Unable to back up $dest; not moving file $src" >&2
          exit 1
        fi
      fi

      destParent=`echo $dest | sed 's:/[^/][^/]*/*$::'`
      createDir $destParent
      if test $? -ne 0; then exit 1; fi

      echo "mv $src $dest"
      mv $src $dest
      if test $? -ne 0; then exit 1; fi

      # Prune an orphaned parent directory, but ignore failures.
      srcParent=`echo $src | sed 's:/[^/][^/]*/*$::'`
      rmdir $srcParent >> /dev/null 2>&1
      exit 0
    else
      echo "Not moving/renaming $src (is not a file)"
    fi
  )
  if test $? -ne 0; then exit 1; fi
}

checkoutCode() {
  if test "$sbmtCodeTop" = ""; then
    echo "Missing location of top of source code tree" >&2
    exit 1
  elif test "$saavtkBranch" = ""; then
    echo "Missing branch definition for saavtk" >&2
    exit 1
  elif test "$sbmtBranch" = ""; then
    echo "Missing branch definition for sbmt" >&2
    exit 1
  fi

  createDir "$sbmtCodeTop"

  markerFile="$sbmtCodeTop/git-succeeded.txt"
  if test ! -f $markerFile;  then
    (
      cd $sbmtCodeTop
      check $?
      chgrp sbmtsw .
      check $?
      chmod 2775 .
      check $?

      echo "In directory $sbmtCodeTop"
      echo "nice git clone http://hardin.jhuapl.edu:8080/scm/git/vtk/saavtk --branch $saavtkBranch > git-clone-saavtk.txt"
      nice git clone http://hardin.jhuapl.edu:8080/scm/git/vtk/saavtk --branch $saavtkBranch > git-clone-saavtk.txt 2>&1
      check $? "Unable to git clone saavtk"

      echo "nice git clone http://hardin.jhuapl.edu:8080/scm/git/sbmt --branch $sbmtBranch > git-clone-sbmt.txt"
      nice git clone http://hardin.jhuapl.edu:8080/scm/git/sbmt --branch $sbmtBranch > git-clone-sbmt.txt 2>&1
      check $? "Unable to git clone sbmt"

      touch $markerFile
    )
    check $? Check-out failed.
  else
    echo "Marker file $markerFile exists already; skipping git clone commands"
  fi
}

buildCode() {
  if test "$sbmtCodeTop" = ""; then
    echo "Missing location of top of source code tree" >&2
    exit 1
  elif test "$SAAVTKROOT" = ""; then
    echo "Missing definition for environment variable SAAVTKROOT" >&2
    exit 1
  elif test "$SBMTROOT" = ""; then
    echo "Missing definition for environment variable SBMTROOT" >&2
    exit 1
  fi

  if test ! -d "$sbmtCodeTop/saavtk"; then
    echo "No such code directory: $sbmtCodeTop/saavtk" >&2
    exit 1
  elif test ! -d "$sbmtCodeTop/sbmt"; then
    echo "No such code directory: $sbmtCodeTop/sbmt" >&2
    exit 1
  fi

  dir="$sbmtCodeTop/sbmt"
  markerFile="$dir/make-release-succeeded.txt"
  if test ! -f $markerFile;  then
    (
      cd "$dir"
      check $?
  
      # Before building, need to set the released mission.
      $dir/misc/scripts/set-released-mission.sh APL_INTERNAL
      check $? "Setting the released mission failed in directory $dir"
  
      # Capture this step in its own log file.
      echo "Building code in $dir; see log $dir/make-release.txt"
      nice make release > make-release.txt 2>&1
      check $? "Make release failed in directory $dir"
  
      touch $markerFile
    )
  else
    echo "Marker file $markerFile exists already; skipping build step"
  fi
}

removeDuplicates() {
  (
    file=$1
    if test "x$file" = x; then
      echo "removeDuplicates was called but missing its argument, the file from which to remove duplicates." >&2
      exit 1
    fi
    if test ! -f $file; then
      echo "removeDuplicates: file $file does not exist." >&2
      exit 1
    fi
    mv $file $file.in
    if test $? -ne 0; then
      echo "removeDuplicates: unable to rename file $file." >&2
      exit 1
    fi
    sort -u $file.in > $file
    if test $? -ne 0; then
      echo "removeDuplicates: unable to remove duplicate lines from file $file." >&2
      exit 1
    fi
    rm -f $file.in
  )
  if test $? -ne 0; then exit 1; fi
}

doGzipDir() {
  (
    dir=$1
    if test "x$dir" = x; then
      echo "Cannot gzip files in missing/blank directory." >&2
      exit 1
    fi
    if test ! -d $dir; then
      echo "Cannot gzip files in $dir: not a directory." >&2
      exit 1
    fi
    for file in $dir/*; do
      if test -f $file; then
        if test `file $file 2>&1 | grep -ic gzip` -eq 0; then
          gzip -cf $file > $file.gz
          if test $? -ne 0; then
            echo "Problem gzipping file $file" >&2
            exit 1
          fi
          rm -f $file
        fi
      fi
    done
  )
  if test $? -ne 0; then exit 1; fi
}

doGzipOptionalDir() {
  (
    dir=$1
    if test "x$dir" = x; then
      echo "Cannot gzip files in missing/blank directory." >&2
      exit 1
    fi
    if test -d $dir; then
      doGzipDir $dir
    fi
  )
  if test $? -ne 0; then exit 1; fi
}

# Copy (rsync) standard model files and directories:
#   aamanifest, basemap/, coloring/, dtm/, shape/
# The only *required* item is aamanifest.txt. If that
# is imissing a fatal error is thrown. The
# rest are copied if present and skipped otherwise
# without an error. 
copyStandardModelFiles() {
  createDir $destTop
  check $?

  # Require a manifest file. Do not comment this out.
  doRsync "$srcTop/aamanifest.txt" "$destTop/aamanifest.txt"

  doRsyncOptionalDir "$srcTop/basemap" "$destTop/shape"
  doRsyncOptionalDir "$srcTop/dtm" "$destTop/dtm"
  doRsyncOptionalDir "$srcTop/coloring" "$destTop/coloring"
  doRsyncOptionalDir "$srcTop/shape" "$destTop/shape"
}

# Run DiscoverPlateColorings.sh, which is linked to a java tool that creates metadata files for plate colorings.
discoverPlateColorings() {
  coloringDir="$destTop/coloring"
  coloringList="coloringlist.txt"
  if test `ls $coloringDir/coloring*.smd 2> /dev/null | wc -c` -eq 0; then
    ls $coloringDir 2> /dev/null | grep -v '.smd' | grep -v '.json' | grep -v "^$coloringList$" > $coloringDir/$coloringList
    
    if test `grep -c . "$coloringDir/$coloringList"` -eq 0; then
      echo "No coloring files found in $coloringDir"
    else
      $sbmtCodeTop/sbmt/bin/DiscoverPlateColorings.sh "$coloringDir" "$outputTopPath/coloring" "$modelId/$bodyId" "$coloringList"
      if test $? -ne 0; then
        echo "Failed to generate plate coloring metadata" >&2
        exit 1
      fi
    fi
    rm -f $coloringDir/$coloringList
  else
    echo "File(s) coloring*.smd exist -- skipping generation of plate coloring metadata"
  fi
}

# Create INFO files from a SPICE metakernel plus a directory with FITS images that have time stamps associated with a keyword.
createInfoFilesFromFITSImages() {
  metakernel=$1
  body=$2
  bodyFrame=$3
  spacecraft=$4
  instrumentFrame=$5
  timeStampKeyword=$6
  imageDir=$7
  infoDir=$8

  # Generate image list with time stamps from the content of the image directory.
  imageTimeStampFile=$(getDirName "$destTop/$imageDir/..")"/imagelist-with-time.txt"
  extractFITSFileTimes $timeStampKeyword $srcTop "$srcTop/$imageDir" $imageTimeStampFile 

  createInfoFilesFromImageTimeStamps $metakernel $body $bodyFrame $spacecraft $instrumentFrame \
    $imageTimeStampFile $infoDir
  check $?
}

# First argument is the keyword associated with time stamps.
# Next argument is the top-level directory; this will be stripped out of the beginning of file names.
# Next argument is the directory to search for FITS files. This must be a descendant of the top-level directory.
# Next argument is the output file that will hold the comma-separated file-name, date/time.
extractFITSFileTimes() {
  timeStampKeyword=$1
  topDir=$(getDirName "$2")
  dir=$(getDirName "$3")
  listFile=$4
  
  if test "$timeStampKeyword" = ""; then
    echo "extractFITSFileTimes: timeStampKeyword argument is blank." >&2
    exit 1
  fi

  if test "$dir" = "$topDir"; then
    relPath=
  elif test `echo $dir | grep -c "$topDir/"` -eq 0; then
    echo "extractFITSFileTimes: top directory $topDir is not an ancestor of directory $dir" >&2
    exit 1
  else
    relPath=`echo $dir | sed "s:^$topDir/::"`
  fi

  if test "$listFile" = ""; then
    echo "extractFITSFileTimes: listFile argument is blank." >&2
    exit 1
  fi

  # Need Ftools for this.
  type ftlist
  if test $? -ne 0; then
    echo "extractFITSFileTimes: need to have Ftools in your path for this to work" >&2
    exit 1
  fi

  createParentDir $listFile
  
  rm -f $listFile
  for file in `ls $dir/` .; do
    if test "$file" != .; then
      # Ftool ftlist prints the whole header line for the keyword, however many times it appears in the file.
      # Parse the first match, assumed to have the standard FITS keyword form:
      # keyname = 'value' / comment
      # In general the comment and the single quotes are not guaranteed to be present, so try to be bullet-proof
      # with the seds. Also the output should have a T rather than space separating the date from the time.
      value=`ftlist "infile=$dir/$file" option=k include=$timeStampKeyword 2> /dev/null | head -1 | \
        sed 's:[^=]*=[  ]*::' | sed 's:[  ]*/.*$::' | sed "s:^''*::" | sed "s:''*$::" | sed 's: :T:'`
      if test $? -eq 0 -a "$value" != ""; then
        echo "$relPath/$file, $value" >> $listFile
      else
        echo "extractFITSFileTimes: unable to extract time; skipping file $file" >&2
      fi
    fi
  done
}

# Create INFO files from a SPICE metakernel plus a CSV file containing a list of images with time stamps.
createInfoFilesFromImageTimeStamps() {
  metakernel=$1
  body=$2
  bodyFrame=$3
  spacecraft=$4
  instrumentFrame=$5
  imageTimeStampFile=$6
  infoDir=$7

  if test "$imageTimeStampFile" = ""; then
    echo "createInfoFilesFromImageTimeStamps: image time stamp file argument is blank." >&2
    exit 1
  elif test ! -f $imageTimeStampFile; then
    echo "createInfoFilesFromImageTimeStamps: image time stamp file $imageTimeStampFile does not exist." >&2
    exit 1
  fi

  if test "$infoDir" = ""; then
    echo "createInfoFilesFromImageTimeStamps: infoDir argument is blank." >&2
    exit 1
  fi

  parentDir=$(getDirName "$infoDir/..")
  imageListFile="$parentDir/imagelist-info.txt"
  imageListFullPathFile="$parentDir/imagelist-fullpath-info.txt"
  missingInfoList="$parentDir/missing-info.txt"

  createInfoFilesDir="$sbmtCodeTop/sbmt/pipeline/rawdata/generic/createInfoFiles"
  if test -d $createInfoFiles; then
    (cd $createInfoFilesDir; check $?; make)
    check $?
  else
    echo "createInfoFilesFromImageTimeStamps: directory $createInfoFilesDir does not exist" >&2
    exit 1
  fi

  createDir "$infoDir"
 
  #  1. metakernel - a SPICE meta-kernel file containing the paths to the kernel files
  #  2. body - IAU name of the target body, all caps
  #  3. bodyFrame - Typically IAU_<body>, but could be something like RYUGU_FIXED
  #  4. spacecraft - SPICE spacecraft name
  #  5. instrumentframe - SPICE instrument frame name
  #  6. imageTimeStampFile - path to CSV file in which all image files are listed (relative
  #     to "topDir") with their UTC time stamps
  #  7. infoDir - path to output directory where infofiles should be saved to
  #  8. imageListFile - path to output file in which all image files for which an infofile was
  #     created (image file name only, not full path) will be listed along with
  #     their start times.
  #  9. imageListFullPathFile - path to output file in which all image files for which an infofile
  #     was created will be listed (full path relative to the server directory).
  # 10. missingInfoList - path to output file in which all image files for which no infofile
  #     was created will be listed, preceded with a string giving the cause for
  #     why no infofile could be created.
  $createInfoFiles $metakernel $body $bodyFrame $spacecraft $instrumentFrame \
    $imageTimeStampFile $infoDir $imageListFile $iamgeListFullPathFile $missingInfoList 2>&1 > create_info_files.txt
}
