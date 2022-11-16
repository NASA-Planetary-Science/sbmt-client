#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:      Russell Turner and Alex Welsh, updated by Josh Steele for Bennu
#                 Further modified by James Peachey for improved portability.
# Description:    Common funcs used by most if not all pipeline scripts
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
      echo "$*" | sed 's:[^ ][^ ]* *::' >&2
    fi
    exit $status
  fi
}

# Get the extension from the string, file name or directory name specified by the first argument.
# The extension is defined as any non-dot characters that follow the final dot in the string.
# If a directory exists with the given name, the extension will be derived from the first file that
# has an extension listed in that directory.
#
# The argument (whether a string, file, or directory) may specify a base name, a relative
# path, a partial path, or an absolute path.
#
# This function only generates an error if the argument is omitted. If the argument specifies an
# empty directory, or a directory whose files have no extensions, an empty string will be
# returned. Similarly, if the argument is a string that does not have an extension (non-period
# characters following a period), an empty string is returned.
guessFileExtension() {
  (
    file=$1
    if test "$file" = ""; then
      check $? "guessFileExtension: missing name of file or directory from which to guess the extension"
    fi

    if test -d "$file"; then
      ext=`ls "$file/"*.* | head -n 1 | sed -n 's:.*\.::p'`
    else
      ext=`echo $file | sed -n 's:.*\.::p'`
    fi
    # Prevent matching a final dot within a directory path or path segment.
    if test `echo $ext | grep -c /` -gt 0; then
      ext=
    fi
    echo $ext
  )
  check $?
}

# Check files in a list of files against a directory for discrepancies. If any files in the list
# are not present in the directory, the function will finish all its checks, but then
# terminate the invoking shell with a non-0 status. Warnings will be generated if files are
# found in the directory that are not present in the list, but excess files of this kind
# would not cause the invoking shell to terminate.
#
# If the directory content exactly matches the list, no output will be produced. Otherwise,
# each discrepancy will be reported on the standard output (not the standard error stream).
#
# This function only writes to the standard error stream if the function itself cannot
# execute properly. In such cases, the function will also exit the invoking shell with a
# non-zero status.
checkFileList() {
  (
    fileList=$1
    dir=$2

    if test "$fileList" = ""; then
      check 1 "checkFileList: missing the list of files to check"
    elif test ! -f "$fileList"; then
      check 1 "checkFileList: argument with list of files to check is not a file: $fileList"
    fi

    if test "$dir" = ""; then
      check 1 "checkFileList: missing the directory to check"
    elif test ! -d "$dir"; then
      check 1 "checkFileList: argument with directory to check is not a directory: $dir"
    fi

    status=0
    firstTime=true
    for file in `cat "$fileList"`; do
      if test ! -e "$dir/$file"; then
        status=1
        if test $firstTime = true; then
          firstTime=false
          echo "ERROR: the following items in the list do not exist in the directory:"
        fi
        echo $file
      fi
    done

    firstTime=true
    for file in `cd "$dir"; ls`; do
      if test `grep -c "^$file" "$fileList"` -eq 0; then
        if test $firstTime = true; then
          firstTime=false
          echo "WARNING: the following items in the directory are not in the list:"
        fi
        echo $file
      fi
    done

    check $status
  )
  check $?
}

# Check files in the sumfiles/ directory against make_sumfiles.in for discrepancies, i.e., listed files not
# found in the directory or files in the directory not listed in make_sumfiles.in.
#
# 1st argument is the path to the imager directory to check.
#
# The 2nd argument is optional; it is the name of an output log file used to store details of
# the check. If no log file is specified, the details will be sent to standard output. The caller
# could also just capture the standard output stream and do whatever with it. The advantage of
# providing the log file as an argument rather than the stream is that this function will automatically
# remove the log file if no discrepancies are found. Error messages still will be sent to the standard
# error stream regardless.
checkSumFiles() {
  (
    imagerDir=$1
    logFile=$2

    if test "$imagerDir" = ""; then
      check 1 "checkSumFiles: missing the directory to check"
    elif test ! -d "$imagerDir"; then
      check 1 "checkSumFiles: argument with directory to check is not a directory: $imagerDir"
    fi

    if test ! -f "$imagerDir/make_sumfiles.in"; then
      check 1 "checkSumFiles: cannot find file $imagerDir/make_sumfiles.in"
    fi

    tmpFileList=checkSumFiles-$(basename "$imagerDir").tmp
    ext=`guessFileExtension $imagerDir/sumfiles`
    sed "s: .*:.$ext:" "$imagerDir/make_sumfiles.in" > $tmpFileList
    check $? "checkSumFiles: could not edit $imagerDir/make_sumfiles.in to create $tmpFileList"

    echo "checkSumFiles: in directory $imagerDir, comparing content of sumfiles/ directory to list in make_sumfiles.in"
    if test "$logFile" != ""; then
      echo "checkSumFiles: in directory $imagerDir, comparing content of sumfiles/ directory to list in make_sumfiles.in" > $logFile
      checkFileList "$tmpFileList" "$imagerDir/sumfiles" >> $logFile
      check $? "checkSumFiles: problems with content of $imagerDir. Files checked listed in $tmpFileList. See details in $logFile"
    else
      checkFileList "$tmpFileList" "$imagerDir/sumfiles"
      check $? "checkSumFiles: problems with content of $imagerDir. Files checked listed in $tmpFileList."
    fi

    # Clean up if no problems were found.
    rm -f $tmpFileList
    if test "$logFile" != ""; then
      if test ! -s $logFile; then
        rm -f $logFile
      fi
    fi
  )
  check $?
}

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

    # When using cp to transfer files, delete the destination each
    # time so that the command always puts the destination in the
    # same place. If the destination exists, cp "helpfully" adds
    # another level, e.g., /blah/blah/coloring/coloring.
    if test `echo $rsyncCmd | grep -c cp` -gt 0; then
      rm -rf $dest
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

      echo "mv $src $dest" >> $log 2>&1
      mv -f $src $dest >> $log 2>&1
      if test $? -ne 0; then exit 1; fi

      # Prune an orphaned parent directory, but ignore failures.
      srcParent=`echo $src | sed 's:/[^/][^/]*/*$::'`
      rmdir $srcParent >> /dev/null 2>&1
      exit 0
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

      echo "mv $src $dest" >> $log 2>&1
      mv $src $dest >> $log 2>&1
      if test $? -ne 0; then exit 1; fi

      # Prune an orphaned parent directory, but ignore failures.
      srcParent=`echo $src | sed 's:/[^/][^/]*/*$::'`
      rmdir $srcParent >> /dev/null 2>&1
      exit 0
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
