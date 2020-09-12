#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:      James Peachey, based on commonFuncs.sh by Russell Turner,
#                 Alex Welsh and Josh Steele.
# Description:
#   Utilities to support processing data in context of
#   importing models, images, spectra etc. into SBMT or similar clients.
#   The functions defined here are designed to fail and quit at the first
#   sign of a problem to prevent costly malfunctions on the server.
#
#   General note: the functions largely make use of the pattern below,
#   which is good for exiting when an error is encountered, and also
#   for encapsulating local variables. This works because curly braces
#   define function body without spawning a sub-shell, but parentheses
#   spawn a sub-shell.
#
# functionName() {
#   # Still in the parent (calling) shell here.
#   (
#     # Now in the sub-shell.
#     localVar=...
#
#     <command> ...
#     check $? "functionName: command had an error; this exits the sub-shell.
#   )
#   # localVar is no longer defined here.
#   check $? # Exits parent shell if $? -ne 0, i.e., if the sub-shell exited
#   with non-0 exit code.
# }
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

# Checks the status code that is passed as the first argument. If it's missing,
# blank or non-0, prints the remaining arguments, which are assumed to
# provide an error message, and then calls exit from within the invoking shell,
# thus terminating the script that called it and returning a non-0 status code.
#
# If the first argument is 0, this function takes no action whatsoever.
#
check() {
  # A missing or blank first argument should be interpreted as an error.
  if test "$1" = ""; then
    # In case the first argument was blank, but additional arguments provide
    # error message, print them before exiting.
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
  check $? $*
}

# Confirm identity of user invoking this command is NOT the sbmt account.
confirmNotSbmt() {
  (
    isSbmt=$(checkIdentity sbmt)
    if test "$isSbmt" != false; then
      check 1
    fi
  )
  check $? $*
}

# Get the absolute path of the directory for the path passed as an argument.
# If the argument identifies an existing directory, this function returns the
# physical absolute path to that directory. Otherwise, if that parent
# directory exists, this function returns the physical absolute path of the
# parent.
#
# This function exits with an error if both the path and its (resolved)
# parent path are not directories.
#
getDirPath() {
  (
    funcName=${FUNCNAME[0]}

    if test "$1" = ""; then
      check 1 "$funcName: directory name is blank."
    fi

    dir="$1"

    if test -d "$dir"; then
      dir=$(realpath "$dir")
      check $? "$funcName: cannot determine path to directory $1"
    else
      dir=$(realpath -m "$dir/..")
      check $? "$funcName: cannot determine path to parent directory of $1"
      
      if test ! -d "$dir"; then
        check 1 "$funcName: parent of $1 does not exist"
      fi
    fi

    echo $dir
  )
  check $?
}

# Get just the filename from a full or partial path. Directories,
# symbolic links and files are all treated like iles, i.e. this really
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
      ext=`ls "$file/"*.* 2> /dev/null | head -n 1 | sed -n 's:.*\.::p'`
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
# If the supplied path does not include the segment "rawdata", this errors out.
#
# This function uses getDirPath to rationalize paths, so the supplied argument
# must actually exist in the file system.
guessRawDataParentDir() {
  if test "$1" = ""; then
    echo "guessRawDataParentDir: no directory supplied as an argument" >&2
    exit 1
  fi

  dir=$(getDirPath "$1")

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
      doRsyncDir $src $dest "$3"
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

# Create a symbolic link after checking whether it may be done without removing a file or
# directory. Existing links (broken or not) are removed and replaced, but existing files
# or directories are NEVER removed.
#
# param target the target path, i.e. the path that locates the thing being linked to
# param linkPath the path to the symbolic link that will be created
#
# Example 1: suppose there is no file/directory/link named bar. Then typing
# "updateLink foo bar" would behave just like "ln -s foo bar".
#
# Example 2: suppose there is a file/directory/link named bar. The same command
# "updateLink foo bar" would, in this case, error out if bar is a file or directory.
# If bar is itself a symbolic link, this command would in effect execute the commands:
# "rm -f bar" and then "ln -s foo bar".
#
createLink() {
  (
    funcName=${FUNCNAME[0]}

    target=$1
    linkPath=$2

    if test "$target" = "" -o "$linkPath" = ""; then
      echo "$funcName: missing argument(s). Need target and linkPath" >&2
      if test $# -gt 1; then
        echo "$*" >&2
      fi
      exit 1
    fi

    if test ! -e $target; then
      echo "$funcName: target file/directory $target does not exist." >&2
      exit 1
    fi

    echo "$funcName: creating symbolic link to $target named $linkPath in $(pwd -P)"

    if test -L $linkPath; then
      # Remove symbolic links to make way for the new link.
      rm -f $linkPath
      check $? "$funcName failed to remove link $linkPath"
    fi

    # Need to be sure linkPath does not already exist before creating a new link.
    if test -e $linkPath; then
      check 1 "$funcName encountered existing item at $linkPath that could not be moved aside or deleted."
    fi

    ln -s $target $linkPath
    status=$?
    if test $status -ne 0; then
      # Failed to create link, so restore backup if possible.
      if test "$backup" != ""; then
        if test -e $backup && ! -e $linkPath; then
          echo "$funcName: restoring $backup to $linkPath"
          mv $backup $linkPath
        fi
      fi
      check $status "$funcName failed to create new link to $target named $linkPath"
    fi
  )
  check $?
}

# Create a symbolic link, first moving aside whatever file/directory/link currently
# exists in the link path. Existing broken links are simply removed. Actual files or
# directories are NEVER removed.
#
# param target the target path, i.e. the path that locates the thing being linked to
# param linkPath the path to the symbolic link that will be created
# param suffix the suffix that will be appended when moving aside an existing link path
#
# Example 1: suppose there is no file/directory/link named bar. Then typing
# "updateLink foo bar redmine-1236" would behave just like "ln -s foo bar".
#
# Example 2: suppose there is a file/directory/link named bar. If bar is a symbolic
# link, assume it is not broken. The same command "updateLink foo bar redmine-1236"
# would, in this case, do the equivalent of two commands:
# "mv bar BACKUP-bar-redmine-1236" and then "ln -s foo bar". However, if
# BACKUP-bar-redmine-1236 exists already, and is not a symbolic link, this function will
# error out, i.e. this function never deletes an actual file or directory.
#
# Example 3: suppose there is a broken link named bar. Then typing
# "updateLink foo bar redmine-1236" would in effect execute the commands:
# "rm -f bar" and then "ln -s foo bar". The broken link will not be backed up, just removed.
#
updateLink() {
  (
    funcName=${FUNCNAME[0]}

    target=$1
    linkPath=$2
    backupSuffix=$3

    if test "$target" = "" -o "$linkPath" = "" -o "$backupSuffix" = ""; then
      echo "$funcName: missing argument(s). Need target, linkPath and backup suffix." >&2
      if test $# -gt 1; then
        echo "$*" >&2
      fi
      exit 1
    fi

    if test ! -e $target; then
      echo "$funcName: target file/directory $target does not exist." >&2
      exit 1
    fi

    echo "$function: updating symbolic link to $target named $linkPath in $(pwd -P)"

    # If linkPath already exists, back it up rather than removing it.
    if test -e $linkPath; then
      linkPathFile=`getFileName "$linkPath"`
      check $? "$funcName failed to get file name"

      linkPathDir=`removePathEnding "$linkPath" "$linkPathFile"`
      check $? "$funcName failed to get directory"

      if test "$linkPathDir" = ""; then
        linkPathDir=.
      fi

      backup="$linkPathDir/BACKUP-$linkPathFile-$backupSuffix"
      # Only back up once.
      if test ! -e $backup; then
        # Backup does not exist or is itself a broken link. Just overwrite it with linkPath.
        mv $linkPath $backup
        check $? "$funcName failed to back up $linkPath"
      elif test -L $linkPath; then
        # Backup exists and the current linkPath is a link that points somewhere. Probably
        # the backup is from a previous call to this function, so leave the backup alone,
        # but delete the current linkPath so that it may be recreated below.
        rm -f $linkPath
        check $? "$funcName failed to remove link $linkPath"
      else
        # Backup points to a file or directory AND linkPath points to a file or directory.
        # This should not ever happen, so do not continue.
        check 1 "$funcName did not expect to find real files/directories in $linkPath and $backup"
      fi
    elif test -L $linkPath; then
      # linkPath is a broken link (test -L returned true but test -e returned false).
      # Remove it, don't bother backing it up.
      rm -f $linkPath
      check $? "$funcName failed to remove link $linkPath"
    fi

    # Need to be sure linkPath does not already exist before creating a new link.
    if test -e $linkPath; then
      check 1 "$funcName encountered existing item at $linkPath that could not be moved aside or deleted."
    fi

    ln -s $target $linkPath
    status=$?
    if test $status -ne 0; then
      # Failed to create link, so restore backup if possible.
      if test "$backup" != ""; then
        if test -e $backup && ! -e $linkPath; then
          echo "$funcName: restoring $backup to $linkPath"
          mv $backup $linkPath
        fi
      fi
      check $status "$funcName failed to create new link to $target named $linkPath"
    fi
  )
  check $?
}

createRelativeLink() {
  (
    funcName=${FUNCNAME[0]}

    target=$1
    if test "$target" = ""; then
      check 1 "$funcName: missing first argument (target for link)"
    fi

    linkPath=$2
    if test "$linkPath" = ""; then
      check 1 "$funcName: missing second argument (the link path)"
    fi

    # Use dirname here to get one level up from the linkPath link name,
    # whether or not it exists yet.
    linkPathDir=$(dirname $linkPath)

    reltarget=`realpath --relative-to=$linkPathDir $target`
    check $? "$funcName: cannot compute relative path to $target from $linkPathDir"

    # Make sure the linkPath directory exists.
    createDir $linkPathDir

    cd $linkPathDir
    check $? "$funcName: cannot cd to linkPath directory $linkPathDir"

    createLink $reltarget $linkPath
  )
  check $?
}

updateRelativeLink() {
  (
    funcName=${FUNCNAME[0]}

    target=$1
    if test "$target" = ""; then
      check 1 "$funcName: missing first argument (target for link)"
    fi

    linkPath=$2
    if test "$linkPath" = ""; then
      check 1 "$funcName: missing second argument (the link path)"
    fi

    # Use dirname here to get one level up from the linkPath link name,
    # whether or not it exists yet.
    linkPathDir=$(dirname $linkPath)

    reltarget=`realpath --relative-to=$linkPathDir $target`
    check $? "$funcName: cannot compute relative path to $target from $linkPathDir"

    # Make sure the linkPath directory exists.
    createDir $linkPathDir

    cd $linkPathDir
    check $? "$funcName: cannot cd to linkPath directory $linkPathDir"

    updateLink $reltarget $linkPath $3
  )
  check $?
}

updateOptionalLink() {
  (
    target=$1

    if test "$target" = ""; then
      echo "updateOptionalLink: missing first argument (target file for link)" >&2
      exit 1
    fi

    if test -e "$target"; then
      updateLink $target $2 $3
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

# Process (rsync and post-process) standard model files and directories:
#   basemap/, shape/
# These are copied if present and skipped otherwise
# without an error. If copied, shape directory is gzipped and
# symbolic links with standardized names to the original shape file
# names are created.
processStandardModelFiles() {
  createDir $destTop
  check $?

  doRsyncOptionalDir "$srcTop/basemap" "$destTop/basemap"
  # DTMs and coloring files are copied during processing now.
#  doRsyncOptionalDir "$srcTop/dtm" "$destTop/dtm"
#  doRsyncOptionalDir "$srcTop/coloring" "$destTop/coloring"
  # Delete other files in the shape directory in case this is a re-start.
  doRsyncOptionalDir "$srcTop/shape" "$destTop/shape" "--delete --links --copy-unsafe-links --keep-dirlinks"
  doGzipOptionalDir "$destTop/shape"

  if test -d "$destTop/shape"; then
    # First argument is directory, second is the prefix
    # for output file name(s).
    createFileSymLinks "$destTop/shape" shape
  fi
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

# First argument is the keyword associated with time stamps.
# Next argument is the top-level directory; this will be stripped out of the beginning of file names.
# Next argument is the directory to search for FITS files. This must be a descendant of the top-level directory.
# Next argument is the output file that will hold the comma-separated file-name, date/time.
extractFITSFileTimes() {
  (
    timeStampKeyword=$1
    topDir=$(getDirPath "$2")
    dir=$(getDirPath "$3")
    listFile=$4

    if test "$timeStampKeyword" = ""; then
      check 1 "extractFITSFileTimes: timeStampKeyword argument is blank."
    fi

    if test "$dir" = "$topDir"; then
      relPath=
    elif test `echo $dir | grep -c "^$topDir/"` -eq 0; then
      check 1 "extractFITSFileTimes: top directory $topDir is not an ancestor of directory $dir"
    else
      relPath=`echo $dir | sed "s:^$topDir/::"`
    fi

    if test "$listFile" = ""; then
      check 1 "extractFITSFileTimes: listFile argument is blank."
    fi

    # Need Ftools for this.
    type ftlist
    if test $? -ne 0; then
      check 1 "extractFITSFileTimes: need to have Ftools in your path for this to work" >&2
    fi

    createParentDir $listFile

    rm -f $listFile
    for file in `ls $dir/ 2> /dev/null` .; do
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
          check $? "extractFITSFileTimes: unable to write to $listFile"
        else
          echo "extractFITSFileTimes: unable to extract time; skipping file $file" >&2
        fi
      fi
    done
  )
  check $?
}

# Create INFO files from a SPICE metakernel plus a CSV file containing a list of images with time stamps.
createInfoFilesFromImageTimeStamps() {
  (
    funcName=${FUNCNAME[0]}

    metakernel=$1
    body=$2
    bodyFrame=$3
    spacecraft=$4
    instrumentFrame=$5
    imageTimeStampFile=$6
    infoDir=$7

    # Must invoke tool from the temporary spice directory in case the metakernel uses relative paths.
    cd $tmpSpiceDir
    check $? "$funcName: unable to cd $tmpSpiceDir"

    if test ! -f $metakernel; then
      check 1 "$funcName: first argument $metakernel is not the path to a metakernel file"
    fi

    if test "$imageTimeStampFile" = ""; then
      check 1 "$funcName: image time stamp file argument is blank."
    elif test ! -f $imageTimeStampFile; then
      check 1 "$funcName: image time stamp file $imageTimeStampFile does not exist."
    fi

    if test "$infoDir" = ""; then
      check 1 "$funcName: infoDir argument is blank."
    fi

    parentDir=$(getDirPath "$destTop/$infoDir/..")

    imageListFile="$parentDir/imagelist-info.txt"
    imageListFullPathFile="$parentDir/imagelist-fullpath-info.txt"
    missingInfoList="$parentDir/missing-info.txt"

    createInfoFilesDir="$sbmtCodeTop/sbmt/pipeline/rawdata/generic/createInfoFiles"
    if test -d $createInfoFiles; then
      (cd $createInfoFilesDir; check $?; make)
      check $?
    else
      check 1 "$funcName: directory $createInfoFilesDir does not exist"
    fi

    createDir "$destTop/$infoDir"

    #  1. metakernel - full path to a SPICE meta-kernel file containing the paths to the kernel files
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

    createDir $logTop

    echo $createInfoFilesDir/createInfoFiles $metakernel $body $bodyFrame $spacecraft $instrumentFrame \
      $imageTimeStampFile "$destTop/$infoDir" $imageListFile $imageListFullPathFile $missingInfoList

    $createInfoFilesDir/createInfoFiles $metakernel $body $bodyFrame $spacecraft $instrumentFrame \
      $imageTimeStampFile "$destTop/$infoDir" $imageListFile $imageListFullPathFile $missingInfoList > \
      $logTop/createInfoFiles.txt 2>&1
    check $? "$funcName: creating info files failed. See log file $logTop/createInfoFiles.txt."
  )
  check $?
}

# Create INFO files from a SPICE metakernel plus a directory with FITS images that have time stamps associated with a keyword.
createInfoFilesFromFITSImages() {
  funcName=${FUNCNAME[0]}

  metakernel=$1
  body=$2
  bodyFrame=$3
  spacecraft=$4
  instrumentFrame=$5
  timeStampKeyword=$6
  imageDir=$7
  infoDir=$8

  if test "$metakernel" = ""; then
    check 1 "$funcName: missing/blank first argument; must be path to metakernel valid in $tmpSpiceDir"
  fi

  if test "$body" = ""; then
    check 1 "$funcName: missing/blank second argument must specify NAIF-compliant body name"
  fi

  if test "$bodyFrame" = ""; then
    check 1 "$funcName: missing/blank third argument must specify NAIF-compliant body frame ID"
  fi

  if test "$spacecraft" = ""; then
    check 1 "$funcName: missing/blank fourth argument must specify NAIF-compliant spacecraft ID"
  fi

  if test "$instrumentFrame" = ""; then
    check 1 "$funcName: missing/blank fifth argument must specify NAIF-compliant instrument frame ID"
  fi

  if test "$timeStampKeyword" = ""; then
    check 1 "$funcName: missing/blank sixth argument must specify keyword used to extract time stamps"
  fi

  if test "$imageDir" = ""; then
    check 1 "$funcName: missing/blank seventh argument must specify image directory relative to $srcTop"
  fi

  if test ! -d "$srcTop/$imageDir"; then
    check 1 "$funcName: seventh argument $imageDir must specify image directory relative to $srcTop"
  fi

  if test "$infoDir" = ""; then
    check 1 "$funcName: missing/blank eighth argument must specify image directory relative to $srcTop"
  fi

  # Generate image list with time stamps from the content of the image directory.
  imageTimeStampDir=$(getDirPath "$destTop/$imageDir/..")

  imageTimeStampFile="$imageTimeStampDir/imagelist-with-time.txt"

  if test ! -f $imageTimeStampFile; then
    extractFITSFileTimes $timeStampKeyword $srcTop "$srcTop/$imageDir" $imageTimeStampFile
  else
    echo "File $imageTimeStampFile exists -- skipping extracting times from FITS images"
  fi

  createInfoFilesFromImageTimeStamps $metakernel $body $bodyFrame $spacecraft $instrumentFrame \
    $imageTimeStampFile $infoDir
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
      checkFileList "$tmpFileList" "$imagerDir/sumfiles" >> $logFile 2>&1
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

# Generate a full set of model metadata files using the client distribution associated with this
# delivery.
#
# param destDir the destination directory under which the metadata files will be created
generateModelMetadata() {
  (
    destDir=$1
    logFile=$logTop/ModelMetadataGenerator.txt

    if test "$destDir" = ""; then
      check 1 "generateModelMetadata: first argument must be target area where to write model metadata."
    fi

    createDir $logTop

    echo "$sbmtCodeTop/sbmt/bin/ModelMetadataGenerator.sh $destDir"
    $sbmtCodeTop/sbmt/bin/ModelMetadataGenerator.sh $destDir >> $logFile 2>&1
    check $? "generateModelMetadata: problems generating metadata. For details, see file $logFile"

  )
  check $?
}

# Infer symbolic link names for arbitrary file names. This is so that the provider can use
# whatever names they want for, say, shape files, but the tool will see shape0, shape1, etc.
createFileSymLinks() {
  (
    dir=$1
    prefix=$2

    if test "$dir" = ""; then
      check 1 "createFileSymLinks: first argument missing; must be directory in which to create symbolic links"
    fi

    if test ! -d $dir; then
      check 1 "createFileSymLinks: $dDir is not a directory"
    fi

    if test "$prefix" = ""; then
      check 1 "createFileSymLinks: second argument missing; must be prefix for symbolic link names"
    fi

    cd $dir
    check $? "createFileSymLinks: unable to cd $dir"

    let res=0
    for file in `ls -Sr * 2> /dev/null | grep -v "^$prefix"` .; do
      if test "$file" != .; then
        lastSuffix=`echo $file | sed -e 's:.*\(\.[^\.]*\)$:\1:'`
        if test $lastSuffix = ".gz"; then
          suffix=`echo $file | sed -e 's:.*\(\.[^\.]*\.[^\.]*\)$:\1:'`
        else
          suffix=$lastSuffix
        fi

        linkName="$prefix${res}$suffix"

        if test $file = $linkName; then
          continue
        fi

        # Remove any previous links.
        rm -f $linkName

        ln -s $file $linkName
        check $? "createFileSymLinks: unable to create symbolic link from $file to $linkName"

        let res=res+1
      fi
    done
  )
  check $?
}

# Unpack any archives found in the provided directory. If none, just skip.
unpackArchives() {
  (
    srcDir=$1

    if test "$srcDir" = ""; then
      check 1 "unpackArchives: missing/blank first argument must be source directory that may contain archive files"
    fi

    if test ! -d "$srcDir"; then
      check 1 "unpackArchives: first argument $srcDir does not specify a source directory that may contain archive files"
    fi

    cd $srcDir
    check $? "unpackArchives: cannot cd $srcDir"

    # Use . to ensure for loop has at least one match.
    for file in `ls *.tar 2> /dev/null` .; do
      if test "$file" != .; then
        tar xf $file
        check $? "unpackArchives: unable to untar file $file"
      fi
    done

    # Use . to ensure for loop has at least one match.
    for file in `ls *.tgz *.tar.gz 2> /dev/null` .; do
      if test "$file" != .; then
        tar zxf $file
        check $? "unpackArchives: unable to untar gzipped file $file"
      fi
    done

  )
  check $?
}

# Edit metakernel files to replace delivered paths with the top of the temporary spice tree.
# param srcDir the directory in which to edit the metakernels
# param regEx the expression to match in the input file(s)
editMetakernels() {
  (
    srcDir=$1

    regEx=$2

    if test "$srcDir" = ""; then
      check 1 "editMetakernels: missing/blank first argument must be source directory that may contain metakernel files"
    fi

    if test ! -d "$srcDir"; then
      check 1 "editMetakernels: first argument $srcDir does not specify a source directory that may contain metakernel files"
    fi

    if test "$regEx" = ""; then
      check 1 "editMetakernels: missing/blank second argument must be regular expression to match delivered paths in metakernels"
    fi

    cd $srcDir
    check $? "editMetakernels: cannot cd $srcDir"

    # Use . to ensure for loop has at least one match.
    for file in `ls *.mk *.tm 2> /dev/null` .; do
      if test "$file" != .; then
        if test ! -f "$file.bak"; then
          sed -i bak -e "s:$regEx:$tmpSpiceDir:"
          check $? "unpackArchives: unable to untar file $file"
        else
          echo "File $file.bak already exists -- not re-editing metakernel file $srcDir/$file"
        fi
      fi
    done

  )
  check $?
}

# Run the database generator to create a table for a particular instrument.
#
# param instrument the instrument identifier, as it is or will be referred to in a Java Instrument object
# param pointing the type of pointing, usually either GASKELL or SPICE
generateDatabaseTable() {
  (
    instrument=$1
    pointing=$2

    if test "$instrument" = ""; then
      check 1 "generateDatabaseTable: missing/blank first argument, which must be the name of an instrument"
    fi

    if test "$pointing" = ""; then
      check 1 "generateDatabaseTable: missing/blank second argument, which must be the pointing type"
    fi

    tool=DatabaseGeneratorSql.sh
    pathToTool=$sbmtCodeTop/sbmt/bin/$tool

    # Just in case, make sure pointing is all uppercase.
    pointing=${pointing^^}

    logFile=$logTop/$tool-$instrument-$pointing.txt

    createDir $logTop

    echo $pathToTool --root-url file://$processedTop --body "${bodyId^^}" --author "$modelId" --instrument "$instrument" $pointing | \
      tee -ai $logFile
    $pathToTool --root-url file://$processedTop --body "${bodyId^^}" --author "$modelId" --instrument "$instrument" $pointing \
      >> $logFile 2>&1
    check $? "generateDatabaseTable: $tool had an error. See log file $logFile"

  )
  check $?
}