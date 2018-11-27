#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:      Russell Turner and Alex Welsh, updated by Josh Steele for Bennu
#                 Further modified by James Peachey for improved portability.
# Description:    Common funcs used by most if not all pipeline scripts
#-------------------------------------------------------------------------------

# Create a directory if it doesn't already exist.
createDirIfNecessary() {
  (
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

    echo nice time $rsyncCmd $src $dest >> $log 2>&1
    nice time $rsyncCmd $src $dest >> $log 2>&1
    if test $? -ne 0; then
      echo "Failed to rsync $src $dest" >> $log 2>&1
      exit 1
    fi

    echo "" >> $log 2>&1
  )
  if test $? -ne 0; then exit 1; fi
}

# Perform an rsync from the source to the destination. Both must be directories.
doRsyncDir() {
  (
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
  )
  if test $? -ne 0; then exit 1; fi
}

# Perform an rsync from a source directory to the destination, but only if the
# source directory exists.
doRsyncDirIfNecessary() {
  (
    src=$1
    dest=$2
    if test -e $src; then
      doRsyncDir $src $dest
    fi
  )
  if test $? -ne 0; then exit 1; fi
}

moveDirectory() {
  (
    src=$1
    dest=$2
    if test "x$src" = x -o "x$dest" = x; then
      echo "Missing argument(s) to moveDirectory" >> $log 2>&1
      exit 1
    fi
    if test -d $src; then
      if test -e $dest; then
        rm -rf $dest-bak
        mv -f $dest $dest-bak
        if test $? -ne 0; then
          echo "Unable to back up $dest; not moving directory $src" >> $log
          exit 1
        fi
      fi
      destParent=`echo $dest | sed 's:/[^/][^/]*/*$::'`
      createDirIfNecessary $destParent
      if test $? -ne 0; then exit 1; fi
      echo "mv $src $dest" >> $log
      mv -f $src $dest >> $log 2>&1
    else
      echo "Not moving/renaming $src (is not a directory)" >> $log
    fi
  )
  if test $? -ne 0; then exit 1; fi
}

moveFile() {
  (
    src=$1
    dest=$2
    if test "x$src" = x -o "x$dest" = x; then
      echo "Missing argument(s) to moveDirectory" >> $log 2>&1
      exit 1
    fi
    if test -f $src; then
      if test -e $dest; then
        rm -rf $dest-bak
        mv $dest $dest-bak
        if test $? -ne 0; then
          echo "Unable to back up $dest; not moving file $src" >> $log
          exit 1
        fi
      fi
      destParent=`echo $dest | sed 's:/[^/][^/]*/*$::'`
      createDirIfNecessary $destParent
      if test $? -ne 0; then exit 1; fi
      echo "mv $src $dest" >> $log
      mv $src $dest >> $log 2>&1
    else
      echo "Not moving/renaming $src (is not a file)" >> $log
    fi
  )
  if test $? -ne 0; then exit 1; fi
}

makeLogDir() {
  (
    if test -e $logDir -a ! -d $logDir; then
      echo "Log directory $logDir exists but is not a directory." >&2
      exit 1
    fi
    mkdir -p $logDir
    if test $? -ne 0; then
      echo "Cannot create log directory $logDir." >&2
      exit 1
    fi
  )
  if test $? -ne 0; then exit 1; fi
}

# Runs in a sub-shell. Indicates success by touching a marker file.
checkoutCodeIfNecessary() (
  (
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
  if test $? -ne 0; then exit 1; fi
)

# Runs in a sub-shell. Indicates success by touching a marker file.
buildCodeIfNecessary() (
  (
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
  if test $? -ne 0; then exit 1; fi
)

removeDuplicates() {
  (
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
  )
  if test $? -ne 0; then exit 1; fi
}

doGzipDir() {
  (
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
  )
  if test $? -ne 0; then exit 1; fi
}

doGzipDirIfNecessary() {
  (
    dir=$1
    if test "x$dir" = x; then
      echo "Cannot gzip files in missing/blank directory." >> $log 2>&1
      exit 1
    fi
    if test -d $dir; then
      doGzipDir $dir
    fi
  )
  if test $? -ne 0; then exit 1; fi
}

# Test code -- do not commit.
#destTop=$PWD
#processingModelName='altwg-spc-v20181116'
#log="$destTop/bozo.log"
#moveDirectory $destTop/$processingModelName/imaging/SUMFILES $destTop/$processingModelName/polycam/SUMFILES
#moveFile $destTop/$processingModelName/imaging/make_sumfiles.in $destTop/$processingModelName/polycam/make_sumfiles.in
