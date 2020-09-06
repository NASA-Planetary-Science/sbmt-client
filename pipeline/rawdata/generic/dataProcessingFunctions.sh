#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:      James Peachey, based on commonFuncs.sh by Russell Turner,
#                 Alex Welsh and Josh Steele.
# Description:    Utilities to support processing data in context of
#                 importing models, images, spectra etc. into SBMT or
#                 similar clients.
#-------------------------------------------------------------------------------

# Check whether the logged in user name is the same as the provided argument.
checkIdentity() {
  if test "$1" = ""; then
    echo "checkIdentity: first argument is blank (should be user name" >&2
    exit 1
  elif test "$1" = `whoami`; then
    echo true
  else
    echo false
  fi
}

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

# Confirm identity of user invoking this command is the sbmt account.
confirmSbmt() {
  (
    isSbmt=$(checkIdentity sbmt)
    if test "$isSbmt" != true; then
      check 1
    fi
  )
  check $?, $*
}

# Confirm identity of user invoking this command is NOT the sbmt account.
confirmNotSbmt() {
  (
    isSbmt=$(checkIdentity sbmt)
    if test "$isSbmt" != false; then
      check 1
    fi
  )
  check $?, $*
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

# Get just the filename from a full or partial path. Directories,
# symbolic links and files are all treated like files, i.e. this really
# returns the final segment. This is done lexically, without checking
# for existence of any part.
getFileName() {
  (
    path=$1
    if test "$path" = ""; then
      check 1 "getFileName: path argument missing/blank"
    fi
  
    # Remove trailing slashes, then everything up to and including a final slash.
    echo $path | sed 's://*$::' | sed 's:.*/::'
  )
  check $?
}

# Remove suffix from a string.
removeSuffix() {
  (
    string=$1
    suffix=$2
    if test "$string" = "" -o "$suffix" = ""; then
      check 1 "removeSuffix: one or more missing arguments"
    fi
  
    result=`echo $string | sed "s:$suffix$::"`
    if test "$result" = "$string"; then
      check 1 "removeSuffix: unable to remove suffix $suffix from string $string"
    fi
  )
  check $?
}

removePathEnding() {
  (
    path=$1
    ending=$2
    if test "$path" = "" -o "$ending" = ""; then
      check 1 "removePathEnding: one or more missing arguments"
    fi
  
    result=`echo $path | sed "s:/*$ending$::"`
    if test "$result" = "$path"; then
      check 1 "removePathEnding: unable to remove ending $ending from path $path"
    fi
    echo $result
  )
  check $?
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

# Perform an rsync from the source (1st argument) to the destination (2nd argument).
# There is an optional 3rd argument, which is extra command line options for rsync.
#
# The rsync command will for sure be invoked with these options:
#   -r recursive
#   -p preserve permissions
#   -t preserve modification times
#   -g preserve group
#   -D preserve "special" files (named sockets and fifos, probably irrelevant)
#   -H preserve hard links. This is unlikely to matter, but is hugely important if
#       it does. If the source area contains 2 hardlinked files and you leave out this
#       option, two copies will be made at the destination.
#
# If the optional 3rd argument to doRsync is omitted, --links --copy-unsafe-links --keep-dirlinks
# (see below) will also be added to the rsync command line.
#
# The 3rd argument, if present, is intended to be a combination of the options below, to control
# how links are handled. It could also be any rsync options that do not conflict with
# the standard options. Note rsync options can be confusing, read descriptions
# carefully.
#
#   --links (-l) means recreate source links in the destination in general. If you omit this,
#       symlinks get skipped completely. Options below (if also supplied) take precedence.
#
#   --copy-links (-L) copy the files/directories that source links point to. Destination will
#       tend to have only files/dirs. Overrides --links completely.
#
#   --copy-unsafe-links copy the files/directories that source links point to, but only for links
#       that point outside the source tree. Does not imply --links for other links.
#
#   --copy-dirlinks (-k) copy directories (and only directories) that source links point to. Does not
#       imply --links for links that point to files.
#
#   --keep-dirlinks (-K). Given a source element that resolves to a directory, if the destination
#       already has a link with the same name, and that link points to a directory, follow that
#       link on the destination side rather than delete the destination link and replace it with
#       a copy of the source directory. For example, supposed a source directory "polycam" corresponds
#       to a destination link polycam -> ../shared/polycam, where ../shared/polycam really is a
#       directory. If --keep-dirlinks is specified, the destination link would be left in place and
#       followed to sync the contents of source "polycam" with ../shared/polycam. Without --keep-dirlinks,
#       the destination link would be deleted, a new polycam directory would be created and *that*
#       would be synced with the source.
#
# This is a lower level function, so it does not check its arguments as carefully, be warned.    
doRsync() {
  (
    src=$1
    dest=$2
    linkOptions=$3

    # Make sure source directories end in a slash.
    if test -d $src; then
      src=`echo $src | sed -e 's:/*$:/:'`
    fi

    if test "$linkOptions" = ""; then
      linkOptions="--links --copy-unsafe-links --keep-dirlinks"
    fi

    rsyncCmd="rsync -rptgDH $linkOptions"
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
    doRsync $src $dest "$3"
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
      doRsyncDir $src $dest  "$3"
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
    doRsync $srcTop/$src $destTop/$dest "$3"
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
    doRsyncDir $srcTop/$src $destTop/$dest "$3"
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
    doRsyncOptionalDir $srcTop/$src $destTop/$dest "$3"
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

# Create a symbolic link to source path ($1) in destination path ($2). Back up
# existing destination paths first, using the processingId ($3) to identify/name the backup.
# Behaves just like ln -s $1 $2 except that if $2 identifies an
# existing file/symbolic link/directory, that item will first be moved aside
# before the link is created.
updateLink() {
  (
    src=$1
    dest=$2
    processingId=$3
    
    if test "$src" = "" -o "$dest" = "" -o "$processingId" = ""; then
      echo "updateLink: missing argument(s). Need source, destination and processing id." >&2
      if test $# -gt 1; then
        echo "$*" >&2
      fi
      exit 1
    fi
  
    if test ! -e $src; then
      echo "updateLink: source file/directory $src does not exist." >&2
      exit 1
    fi
  
    # If destination already exists, back it up rather than removing it outright.
    if test -e $dest; then
      destFile=`getFileName "$dest"`
      check $? "updateLink failed to get file name"

      destDir=`removePathEnding "$dest" "$destFile"`
      check $? "updateLink failed to get directory"

      if test "$destDir" = ""; then
        destDir=.
      fi

      backup="$destDir/.$destFile-bak-$processingId"
      # Only back up once.
      if test ! -e $backup; then
        mv $dest $backup
        check $? "updateLink failed to back up $dest"
      elif test -L $dest; then
        # This symbolic link is probably from an earlier invocation of updateLink - remove it.
        rm -f $dest
        check $? "updateLink failed to remove link $dest"
      else
        # A file or directory AND what appears to be a back-up both exist here.
        # This should not ever happen, so do not continue.
        check 1 "updateLink did not expect to find both a real file $dest and a backup $backup"
      fi
    elif test -L $dest; then
      # Destination is a broken link (test -L returned true but test -e returned false).
      # Remove it.
      rm -f $dest
    fi

    ln -s $src $dest
    status=$?
    if test $status -ne 0; then
      # Failed to create link, so restore backup.
      if test "$backup" != ""; then
        mv $backup $dest
      fi
      check $status "updateLink failed to create new link"
    fi
  )
  check $?
}

updateRelativeLink() {
  (
    src=$1
    if test "$src" = ""; then
      check 1 "updateRelativeLink: missing first argument (source file for link)"
    fi

    dest=$2
    if test "$dest" = ""; then
      check 1 "updateRelativeLink: missing second argument (destination file for link)"
    fi

    processingId=$3
    if test "$processingId" = ""; then
      check 1 "updateRelativeLink: missing processingID for the link"
    fi

    destDir=$(getDirName $dest)

    relSrc=`realpath --relativeTo=$destDir $src`
    check $? "updateRelativeLink: cannot compute relative path to $src from $destDir"

    updateLink $relSrc $dest $processingId
  )
  check $?
}

updateOptionalLink() {
  (
    src=$1

    if test "$src" = ""; then
      echo "updateOptionalLink: missing first argument (source file for link)" >&2
      exit 1
    fi

    if test -e "$src"; then
      updateLink $src $2 $3
      check $?
    fi
  )
  check $?
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

# Copy (rsync) standard model files and directories:
#   basemap/, shape/
# These are copied if present and skipped otherwise
# without an error. 
copyStandardModelFiles() {
  createDir $destTop
  check $?

  doRsyncOptionalDir "$srcTop/basemap" "$destTop/basemap"
  # DTMs and coloring files are copied during processing now.
#  doRsyncOptionalDir "$srcTop/dtm" "$destTop/dtm"
#  doRsyncOptionalDir "$srcTop/coloring" "$destTop/coloring"
  doRsyncOptionalDir "$srcTop/shape" "$destTop/shape"
}

# This made a "deployed" model directory with no info about
# the processing ID in the name, but containing links to each
# specific part of the model in the delivered real directory
# (that does include processing Id(. 
# Deprecated. Only one top-level link is ever made.
linkStandardModelFiles() {
  (
    createDir $destTop
    check $?

    cd $destTop
    check $?

    echo "In $destTop, trying to link $srcTop/shape etc."

    updateOptionalLink "$srcTop/basemap" "basemap" "$processingId"
    updateOptionalLink "$srcTop/dtm" "dtm" "$processingId"
    updateOptionalLink "$srcTop/coloring" "coloring" "$processingId"
    updateOptionalLink "$srcTop/shape" "shape" "$processingId"
  
  )
  check $? "linkStandardModelFiles failed"
}

# List plate coloring files in the preferred sort order.
listPlateColoringFiles() {
  (
    coloringDir=$1
    if test "$coloringDir" = ""; then
      check 1 "listPlateColoringFiles coloringDir argument is missing"
    fi
  
    if test ! -d $coloringDir; then
      check 1 "listPlateColoringFiles first argument must be directory where coloring files are found"
    fi
  
    listFile=$2
    if test "$listFile" = ""; then
      check 1 "listPlateColoringFiles listFile argument is missing"
    fi

    rm -f $listFile
    check $? "listPlateColorfingFiles could not remove list file $listFile"

    ls $coloringDir/Slope* >> $listFile 2> /dev/null
    ls $coloringDir/*slp* >> $listFile 2> /dev/null
    ls $coloringDir/Elevation* >> $listFile 2> /dev/null
    ls $coloringDir/*elv* >> $listFile 2> /dev/null
    ls $coloringDir/GravitationalAcceleration* >> $listFile 2> /dev/null
    ls $coloringDir/*grm* >> $listFile 2> /dev/null
    ls $coloringDir/GravitationalPotential* >> $listFile 2> /dev/null
    ls $coloringDir/*pot* >> $listFile 2> /dev/null
    ls $coloringDir/*fti* >> $listFile 2> /dev/null
    ls $coloringDir/*fdi* >> $listFile 2> /dev/null
    ls $coloringDir/*mti* >> $listFile 2> /dev/null
    ls $coloringDir/*tiv* >> $listFile 2> /dev/null
    ls $coloringDir/*mdi* >> $listFile 2> /dev/null
    ls $coloringDir/*div* >> $listFile 2> /dev/null
    ls $coloringDir/*rti* >> $listFile 2> /dev/null
    ls $coloringDir/*rdi* >> $listFile 2> /dev/null
    ls $coloringDir/*mht* >> $listFile 2> /dev/null
    ls $coloringDir/*grv* >> $listFile 2> /dev/null
    ls $coloringDir/*nvf* >> $listFile 2> /dev/null
    # ls $coloringDir/*alb* >> $listFile 2> /dev/null
    # ls $coloringDir/*are* >> $listFile 2> /dev/null
    # ls $coloringDir/*rad* >> $listFile 2> /dev/null

    # List everything that didn't match one of the above patterns in its natural sort order.
    ls $coloringDir/* 2> /dev/null | grep -v Slope | grep -v slp | grep -v Elevation | grep -v elv | \
       grep -v GravitationalAcceleration | grep -v grm | grep -v GravitationalPotential | \
       grep -v pot | grep -v fti | grep -v fdi | grep -v mti | grep -v tiv | grep -v mdi | \
       grep -v div | grep -v rti | grep -v rdi | grep -v mht | grep -v grv | grep -v nvf | sort \
          >> $listFile 2> /dev/null
    # Remove paths into a temporary file.
    sed 's:.*/::' $listFile > $listFile-tmp
    
    # Pingpong back from temp file to final list file, stripping out non-coloring files that might have gotten mixed in.
    cat $listFile-tmp | grep -v '\.smd' | grep -v '\.json' | grep -v $(getFileName $listFile) > $listFile

    # Remove temp file.
    rm -f $listFile-tmp
  )
  check $? "listPlateColoringFiles failed"
}

# Run DiscoverPlateColorings.sh, which is linked to a java tool that creates metadata files for plate colorings.
discoverPlateColorings() {
  (
    src=$srcTop/coloring
    if test -d $src; then
      dest=$destTop/coloring
      
      doRsyncDir $src $dest "$1"
  
      if test `ls $dest/coloring*.smd 2> /dev/null | wc -c` -eq 0; then
        doGzipDir $dest
    
        coloringList="coloringlist.txt"    
        listPlateColoringFiles $dest $dest/$coloringList
        
        if test -s $dest/$coloringList; then
          $sbmtCodeTop/sbmt/bin/DiscoverPlateColorings.sh "$dest" "$outputTop/coloring" "$modelId/$bodyId" "$coloringList"
          check $? "Failed to generate plate coloring metadata"
        else
          echo "No coloring files found in $dest"
        fi
        rm -f $dest/$coloringList
      else
        echo "File(s) coloring*.smd exist -- skipping generation of plate coloring metadata"
      fi
    else
      echo "discoverPlateColorings: nothing to process; no source directory $src"
    fi
  )
  check $? "discoverPlateColorings failed"
}

processDTMs() {
  (
    src=$srcTop/dtm
    if test -d $src; then
      dest=$destTop/dtm/browse
  
      doRsyncDir $src $dest "$1"
  
      fileList="fileList.txt"
      (cd $dest; ls | sed 's:\(.*\):\1\,\1:' | grep -v $fileList > $fileList)
      check $? "processDTMs: problem creating DTM file list $dest/$fileList"
  
      if test ! -s $dest/$fileList; then
        echo "processDTMs: directory exists but has no DTMs: $dest"
      fi
    else
      echo "processDTMs: nothing to process; no source directory $src"
    fi  
  )
  check $? "processDTMs failed"
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

  if test ! -f $imageTimeStampFile; then
    extractFITSFileTimes $timeStampKeyword $srcTop "$srcTop/$imageDir" $imageTimeStampFile 
  else
    echo "File $imageTimeStampFile exists -- skipping extracting times from FITS images"
  fi

  createInfoFilesFromImageTimeStamps $metakernel $body $bodyFrame $spacecraft $instrumentFrame \
    $imageTimeStampFile $infoDir
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
  elif test `echo $dir | grep -c "^$topDir/"` -eq 0; then
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

  parentDir=$(getDirName "$destTop/$infoDir/..")
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

  createDir "$destTop/$infoDir"
 
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

  echo $createInfoFilesDir/createInfoFiles $destTop/$metakernel $body $bodyFrame $spacecraft $instrumentFrame \
    $imageTimeStampFile "$destTop/$infoDir" $imageListFile $imageListFullPathFile $missingInfoList

  $createInfoFilesDir/createInfoFiles $destTop/$metakernel $body $bodyFrame $spacecraft $instrumentFrame \
    $imageTimeStampFile "$destTop/$infoDir" $imageListFile $imageListFullPathFile $missingInfoList 2>&1 > create_info_files.txt
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

