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

# Check whether to skip this section. Warning, this exits the calling
# shell. If any arguments are supplied, they are printed.
checkSkip() {
  if test "$skipSection" = "true"; then
    if test $# -gt 0; then
      echo "Skipping $*" >&2
    fi
    exit 0
  fi
}

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

# Checks the status code that is passed as the first argument. If it is missing,
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
  # does not contain an integer. A simple if test... does not behave reliably.
  # To make matters worse, exit will not actually terminate the invoking shell
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
    funcName=${FUNCNAME[0]}

    isSbmt=$(checkIdentity sbmt)
    if test "$isSbmt" != true; then
      check 1 "$funcName: user name is not sbmt"
    fi
  )
  check $? $*
}

# Confirm identity of user invoking this command is NOT the sbmt account.
confirmNotSbmt() {
  (
    funcName=${FUNCNAME[0]}

    isSbmt=$(checkIdentity sbmt)
    if test "$isSbmt" != false; then
      check 1 "$funcName: user name is sbmt"
    fi
  )
  check $? $*
}

# Get the absolute path of the directory for the path passed as an argument.
# If the argument identifies an existing file, the parent directory of that
# file will be returned. Otherwise, this function returns the physical
# absolute path to that directory.
#
getDirPath() {
  (
    funcName=${FUNCNAME[0]}

    # This function returns a value, so it does not checkSkip.
    # checkSkip $funcName "$*"

    if test "$1" = ""; then
      check 1 "$funcName: directory name is blank."
    fi

    dir="$1"

    if test -f "$dir"; then
      dir=$(realpath -m "$dir/..")
      check $? "$funcName: cannot determine path to parent directory of $1"
    else
      dir=$(realpath -m "$dir")
      check $? "$funcName: cannot determine path from the string $1"
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
  (
    funcName=${FUNCNAME[0]}

    if test "$1" = ""; then
      check 1 "$funcName: no directory supplied as an argument"
    fi

    dir=$(getDirPath "$1")

    # Extract everything to the right of right-most rawdata/.
    suffix=`echo $dir | sed -n 's:.*/rawdata/::p'`

    if test "$suffix" != ""; then
      result=`echo $dir | sed "s:/rawdata/$suffix$::"`
    elif test `echo $dir | grep -c '/rawdata$'` -gt 0; then
      result=`echo $dir | sed "s:/rawdata$::"`
    else
      check 1 "$funcName: can't guess rawdata location from $dir"
    fi

    echo $result
  )
  check $?
}

# Create a directory if it does not already exist.
createDir() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    dir=$1
    if test "$dir" = ""; then
      check 1 "$funcName: missing/blank directory argument."
    fi

    if test ! -d "$dir"; then
      echo "mkdir -p $dir"
      mkdir -p "$dir"
      check $? "$funcName: unable to create directory $dir."
    fi
  )
  check $?
}

# Create the parent of a file/directory if it does not already exist.
createParentDir() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    dir=$1
    if test "$dir" = ""; then
      check 1 "$funcName: missing/blank directory argument."
    fi

    parentDir=$(dirname "$dir")"/.."
    if test ! -d "$parentDir"; then
      echo "mkdir -p $parentDir"
      mkdir -p "$parentDir"
      check $? "$funcName: unable to create directory $parentDir."
    fi
  )
  check $?
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
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

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
    check $? "$funcName: failed to rsync $src $dest"

    echo ""
  )
  check $?
}

# Perform an rsync from the source to the destination. Both must be directories.
doRsyncDir() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    src=$1
    dest=$2
    if test ! -e $src; then
      check 1 "$funcName: source $src does not exist"
    fi
    if test ! -d $src; then
      check 1 "$funcName: source $src is unexpectedly not a directory."
    fi
    if test -e $dest -a ! -d $dest; then
      check 1 "$funcName: destination $dest exists but is unexpectedly not a directory."
    fi
    createDir $dest
    doRsync $src $dest "$3"
  )
  check $?
}

# Perform an rsync from a source directory to the destination, but only if the
# source directory exists.
doRsyncOptionalDir() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    src=$1
    dest=$2
    # Check only existence here. doRsyncDir will take care of
    # reporting error if src is not a directory.
    if test -e "$src"; then
      doRsyncDir $src $dest "$3"
    fi
  )
  check $?
}

# Set srcTop and destTop to point to source and destination top location.
# Then call this, passing src and dest relative to these "top" directories.
copyFile() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    if test "$1" = ""; then
      check 1 "$funcName: source file name is not set"
    fi
    src=$1
    if test "$2" = ""; then
      dest=$src
    else
      dest=$2
    fi

    if test "$srcTop" = ""; then
      check 1 "$funcName: global variable srcTop is not set"
    fi
    if test "$destTop" = ""; then
      check 1 "$funcName: global variable destTop is not set"
    fi
    doRsync $srcTop/$src $destTop/$dest "$3"
  )
  check $?
}

# Copy a directory. Throws an error and quits if source is missing.
# Set srcTop and destTop to point to source and destination top location.
# Then call this, passing src and dest relative to these "top" directories.
copyDir() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    if test "$1" = ""; then
      check 1 "$funcName: source directory name is not set"
    fi
    src=$1
    if test "$2" = ""; then
      dest=$src
    else
      dest=$2
    fi

    if test "$srcTop" = ""; then
      check 1 "$funcName: global variable srcTop is not set"
    fi
    if test "$destTop" = ""; then
      check 1 "$funcName: global variable destTop is not set"
    fi
    doRsyncDir $srcTop/$src $destTop/$dest "$3"
  )
  check $?
}

# Copy a directory, but only if the source directory exists. No error, no op if it is missing.
# Set srcTop and destTop to point to source and destination top location.
# Then call this, passing src and dest relative to these "top" directories.
copyOptionalDir() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    if test "$1" = ""; then
      check 1 "$funcName: source directory name is not set"
    fi
    src=$1
    if test "$2" = ""; then
      dest=$src
    else
      dest=$2
    fi

    if test "$srcTop" = ""; then
      check 1 "$funcName: global variable srcTop is not set"
    fi
    if test "$destTop" = ""; then
      check 1 "$funcName: global variable destTop is not set"
    fi
    doRsyncOptionalDir $srcTop/$src $destTop/$dest "$3"
  )
  check $?
}

moveDirectory() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    src=$1
    dest=$2
    if test "$src" = "" -o "$dest" = ""; then
      check 1 "$funcName: missing argument(s) to moveDirectory"
    fi
    if test -d $src; then
      if test -e $dest; then
        rm -rf $dest-bak
        mv -f $dest $dest-bak
        check $? "$funcName: unable to back up $dest; not moving directory $src"
      fi

      destParent=`echo $dest | sed 's:/[^/][^/]*/*$::'`
      createDir $destParent

      echo "nice mv $src $dest"
      nice mv -f $src $dest
      check $?

      # Prune an orphaned parent directory, but ignore failures.
      srcParent=`echo $src | sed 's:/[^/][^/]*/*$::'`
      rmdir $srcParent >> /dev/null 2>&1
      exit 0
    else
      echo "$funcName: not moving/renaming $src (is not a directory)"
    fi
  )
  check $?
}

moveFile() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    src=$1
    dest=$2
    if test "$src" = "" -o "$dest" = ""; then
      check 1 "$funcName: missing argument(s) to moveDirectory"
    fi
    if test -f $src; then
      if test -e $dest; then
        rm -rf $dest-bak
        mv $dest $dest-bak
        check $? "$funcName: unable to back up $dest; not moving file $src"
      fi

      destParent=`echo $dest | sed 's:/[^/][^/]*/*$::'`
      createDir $destParent

      echo "nice mv $src $dest"
      nice mv $src $dest
      check $?

      # Prune an orphaned parent directory, but ignore failures.
      srcParent=`echo $src | sed 's:/[^/][^/]*/*$::'`
      rmdir $srcParent >> /dev/null 2>&1
      exit 0
    else
      echo "$funcName: not moving/renaming $src (is not a file)"
    fi
  )
  check $?
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

    checkSkip $funcName "$*"

    target=$1
    linkPath=$2

    if test "$target" = "" -o "$linkPath" = ""; then
      check 1 "$funcName: missing argument(s). Need target and linkPath"
    fi

    if test ! -e $target; then
      check 1 "$funcName: target file/directory $target does not exist."
    fi

    echo "$funcName: creating symbolic link to $target named $linkPath in $(pwd -P)"

    if test -L $linkPath; then
      # Remove symbolic links to make way for the new link.
      rm -f $linkPath
      check $? "$funcName: failed to remove link $linkPath"
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

    checkSkip $funcName "$*"

    target=$1
    linkPath=$2
    backupSuffix=$3

    if test "$target" = "" -o "$linkPath" = "" -o "$backupSuffix" = ""; then
      check 1 "$funcName: missing argument(s). Need target, linkPath and backup suffix."
    fi

    if test ! -e $target; then
      check 1 "$funcName: target file/directory $target does not exist."
    fi

    echo "$funcName: updating symbolic link to $target named $linkPath in $(pwd -P)"

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
      # Remove it, do not bother backing it up.
      rm -f $linkPath
      check $? "$funcName failed to remove link $linkPath"
    fi

    # Need to be sure linkPath does not already exist before creating a new link.
    if test -e $linkPath; then
      check 1 "$funcName encountered existing item at $linkPath that could not be moved aside or deleted."
    fi

    echo "ln -s $target $linkPath"
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

    checkSkip $funcName "$*"

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

    # Make sure the linkPath directory exists. Must do this before using realpath.
    createDir $linkPathDir

    reltarget=`realpath --relative-to=$linkPathDir $target`
    check $? "$funcName: cannot compute relative path to $target from $linkPathDir"

    cd $linkPathDir
    check $? "$funcName: cannot cd to linkPath directory $linkPathDir"

    createLink $reltarget $linkPath
  )
  check $?
}

updateRelativeLink() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

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

    # Make sure the linkPath directory exists. Must do this before using realpath.
    createDir $linkPathDir

    reltarget=`realpath --relative-to=$linkPathDir $target`
    check $? "$funcName: cannot compute relative path to $target from $linkPathDir"

    cd $linkPathDir
    check $? "$funcName: cannot cd to linkPath directory $linkPathDir"

    updateLink $reltarget $linkPath $3
  )
  check $?
}

updateOptionalLink() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    target=$1

    if test "$target" = ""; then
      check 1 "$funcName: missing first argument (target file for link)"
    fi

    if test -e "$target"; then
      updateLink $target $2 $3
      check $?
    fi
  )
  check $?
}

createHardLinks() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    src=$1
    dest=$2

    if test "$src" = ""; then
      check 1 "$funcName: first argument (source) is missing or blank"
    fi

    if test "$dest" = ""; then
      check 1 "$funcName: second argument (destination) is missing or blank"
    fi

    if test ! -e "$src"; then
      check 1 "$funcName: source file/directory does not exist: $src"
    fi

    if test -e "$dest"; then
      check 1 "$funcName: destination file/directory already exists: $dest"
    fi

    createParentDir $dest

    echo "nice cp -al $src $dest"
    nice cp -al $src $dest
    check $? "$funcName: unable to link $src to $dest"
  )
  check $?
}

doGzipDir() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    dir=$1
    if test "$dir" = ""; then
      check 1 "$funcName: cannot gzip files in missing/blank directory."
    fi
    if test ! -d $dir; then
      check 1 "$funcName: cannot gzip files in $dir: not a directory."
    fi
    for file in $dir/*; do
      if test -f $file; then
        if test `file $file 2>&1 | grep -ic gzip` -eq 0; then
          echo "nice gzip -cf $file > $file.gz"
          nice gzip -cf $file > $file.gz
          check $? "$funcName: problem gzipping file $file"
          rm -f $file
        fi
      fi
    done
  )
  check $?
}

doGzipOptionalDir() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    dir=$1
    if test "$dir" = ""; then
      check 1 "$funcName: cannot gzip files in missing/blank directory."
    fi
    if test -d $dir; then
      doGzipDir $dir
    fi
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
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    fileList=$1
    dir=$2

    if test "$fileList" = ""; then
      check 1 "$funcName: missing the list of files to check"
    elif test ! -f "$fileList"; then
      check 1 "$funcName: argument with list of files to check is not a file: $fileList"
    fi

    if test "$dir" = ""; then
      check 1 "$funcName: missing the directory to check"
    elif test ! -d "$dir"; then
      check 1 "$funcName: argument with directory to check is not a directory: $dir"
    fi

    status=0
    firstTime=true
    for file in `cat "$fileList"`; do
      if test ! -e "$dir/$file"; then
        status=1
        if test $firstTime = true; then
          firstTime=false
          echo "$funcName: ERROR -- the following items in the list do not exist in the directory:"
        fi
        echo $file
      fi
    done

    firstTime=true
    for file in `cd "$dir"; ls`; do
      if test `grep -c "^$file" "$fileList"` -eq 0; then
        if test $firstTime = true; then
          firstTime=false
          echo "$funcName: WARNING -- the following items in the directory are not in the list:"
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
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    createDir $destTop

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
  )
  check $?
}

processShapeModels() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    src=$1

    if test "$src" = ""; then
      check 1 "$funcName: first argument (model directory) is missing or blank"
    fi

    dest="$destTop/$src"

    if test ! -d "$dest"; then
      check 1 "$funcName: source file/directory does not exist: $dest"
    fi

    if test -d "$dest"; then
      echo "$funcName: trying to zip and make links at $dest"

      doGzipDir "$dest"

      # First argument is directory, second is the prefix
      # for output file name(s).
      createFileSymLinks "$dest" shape
    fi
  )
  check $?
}

# This made a "deployed" model directory with no info about
# the processing ID in the name, but containing links to each
# specific part of the model in the delivered real directory
# (that does include processing Id).
# Deprecated. Only one top-level link is ever made.
linkStandardModelFiles() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    createDir $destTop

    cd $destTop
    check $?

    echo "$funcName: in $destTop, trying to link $srcTop/shape etc."

    updateOptionalLink "$srcTop/basemap" "basemap" "$processingId"
    updateOptionalLink "$srcTop/dtm" "dtm" "$processingId"
    updateOptionalLink "$srcTop/coloring" "coloring" "$processingId"
    updateOptionalLink "$srcTop/shape" "shape" "$processingId"

  )
  check $? "$funcName failed"
}

# List plate coloring files in the preferred sort order.
listPlateColoringFiles() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    coloringDir=$1
    if test "$coloringDir" = ""; then
      check 1 "$funcName coloringDir argument is missing"
    fi

    if test ! -d $coloringDir; then
      check 1 "$funcName first argument must be directory where coloring files are found"
    fi

    listFile=$2
    if test "$listFile" = ""; then
      check 1 "$funcName listFile argument is missing"
    fi

    rm -f $listFile
    check $? "$funcName could not remove list file $listFile"

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

    # List everything that did not match one of the above patterns in its natural sort order.
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
  check $? "$funcName failed"
}

# Set up plate colorings, if the directory coloring is present under the top model directory. This requires
# some variables to be set:
#   bodyId body identifier as it appears in the menu of the client
#   modelId model identifier as it appears in the menu of the client
#   outputTop path prefix, i.e., body-dir/model-dir/coloring
# Run DiscoverPlateColorings.sh, which is linked to a java tool that creates metadata files for plate colorings.
discoverPlateColorings() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    if test "$bodyId" = ""; then
      check 1 "$funcName: variable bodyId is not set"
    fi

    if test "$modelId" = ""; then
      check 1 "$funcName: variable modelId is not set"
    fi

    if test "$outputTop" = ""; then
      check 1 "$funcName: variable outputTop (should be body-dir/model-dir) is not set"
    fi

    src=$srcTop/coloring
    if test -d $src; then
      dest=$destTop/coloring

      doRsyncDir $src $dest "$1"

      if test `ls $dest/coloring*.smd 2> /dev/null | wc -c` -eq 0; then
        doGzipDir $dest

        coloringList="coloringlist.txt"
        listPlateColoringFiles $dest $dest/$coloringList

        if test -s $dest/$coloringList; then
          echo "nice $sbmtCodeTop/sbmt/bin/DiscoverPlateColorings.sh $dest $outputTop/coloring $modelId/$bodyId $coloringList"
          nice $sbmtCodeTop/sbmt/bin/DiscoverPlateColorings.sh "$dest" "$outputTop/coloring" "$modelId/$bodyId" "$coloringList"
          check $? "$funcName: failed to generate plate coloring metadata"
        else
          echo "$funcName: no coloring files found in $dest"
        fi
        rm -f $dest/$coloringList
      else
        echo "$funcName: file(s) coloring*.smd exist -- skipping generation of plate coloring metadata"
      fi
    else
      echo "$funcName: nothing to process; no source directory $src"
    fi
  )
  check $? "$funcName failed"
}

processDTMs() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    generateCatalog="/homes/lopeznr1/DemCatalogMaker-2020.11.17/runDemCatalogMaker --fileL browse/*obj browse/*fits --dispName Browse --destFile browse.cat.csv"

    dest="$destTop/dtm/browse"
    if test -d $dest; then

      if test `ls $dest | wc` -gt 0; then
        cd "$destTop/dtm"
        check $? "$funcName: unable to change directory to the DTM area $destTop/dtm"

        logFile="$logTop/runDemCatalogMaker.txt"

        $catalogTool > $logFile 2>&1
        check $? "$funcName: catalog tool failed to run in $destTop/src. See file $logFile"
      else
        echo "$funcName: no DTM files present in $dest"
      fi
    else
      echo "$funcName: no DTM directory $dest"
    fi
  )
  check $? "$funcName failed"
}

# First argument is the keyword associated with time stamps.
# Next argument is the top-level directory; this will be stripped out of the beginning of file names.
# Next argument is the directory to search for FITS files. This must be a descendant of the top-level directory.
# Next argument is the output file that will hold the comma-separated file-name, date/time.
extractFITSFileTimes() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    timeStampKeyword=$1
    topDir=$(getDirPath "$2")
    dir=$(getDirPath "$3")
    listFile=$4

    if test "$timeStampKeyword" = ""; then
      check 1 "$funcName: timeStampKeyword argument is blank."
    fi

    if test "$dir" = "$topDir"; then
      relPath=
    elif test `echo $dir | grep -c "^$topDir/"` -eq 0; then
      check 1 "$funcName: top directory $topDir is not an ancestor of directory $dir"
    else
      relPath=`echo $dir | sed "s:^$topDir/::"`
    fi

    if test "$listFile" = ""; then
      check 1 "$funcName: listFile argument is blank."
    fi

    # Need Ftools for this.
    type ftlist
    check $? "$funcName: need to have Ftools in your path for this to work"

    createParentDir $listFile

    rm -f $listFile
    for file in `ls $dir/ 2> /dev/null` .; do
      if test "$file" != .; then
        # Ftool ftlist prints the whole header line for the keyword, however many times it appears in the file.
        # Parse the first match, assumed to have the standard FITS keyword form:
        # keyname = 'value' / comment
        # In general the comment and the single quotes are not guaranteed to be present, so try to be bullet-proof
        # with the seds. Also the output should have a T rather than space separating the date from the time.
        value=`nice ftlist "infile=$dir/$file" option=k include=$timeStampKeyword 2> /dev/null | head -1 | \
          sed 's:[^=]*=[  ]*::' | sed 's:[  ]*/.*$::' | sed "s:^''*::" | sed "s:''*$::" | sed 's: :T:'`
        check $? "$funcName: ftlist command failed to extract time from file $dir/$file"

        # If the keyword is not present, the above command ends up with no time in it but does not return non-0 status.
        # Confirm the value at least starts with a numeral.
        if test `echo $value | grep -c '^[0-9]'` -eq 0; then
          check 1 "$funcName: was unable to get a time for keyword $timeStampKeyword from file $dir/$file"
        fi

        echo "$relPath/$file, $value" >> $listFile
        check $? "$funcName: unable to write to $listFile"

      fi
    done
  )
  check $?
}

# For the imaging instrument directory specified by the argument, create a gallery list file that gives the name of each
# main image along with its preview (thumbnail) and its  gallery image, in that order. This function
# only pays attention to the image files and their corresponding gallery files; it has nothing to do
# with pointing. It does not rely on any preexisting image list files.
#
# This function also creates a zip file that holds the gallery files to allow more efficient gallery access
# by the client. Note that if there are a very large number of gallery files, this may result in a very
# large zip file. Because of this, in future, may need/want an option to disable this.
#
# Assumptions:
#   1. Main images are under "images", gallery images are under "gallery".
#   2. The gallery subdirectory is laid out parallel to the images subdirectory.
#   3. For each main image, the gallery directory includes a preview image and a gallery image.
#   4. The name of each preview and gallery image BEGINS WITH its corresponding complete image file name. (This could
#      be generalized).
#
# Most of this function does not assume any particular layout under images, that is, it could be flat or
# hierarchical. The exception is that this function starts by creating a flat list of images, so this step
# would need to be generalized if a hierarchical layout were used.
#
# @param instrumentTop the full path to the top directory for the imaging instrument, e.g., dart/draco or didymos/ideal_impact1.../draco.
createGalleryList() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    instrumentTop=$1

    if test "$instrumentTop" = ""; then
      check 1 "$funcName: missing/blank first argument must specify the full path to the imaging instrument top level directory"
    fi

    if test ! -d "$instrumentTop"; then
      check 1 "$funcName: first argument $instrumentTop is not a directory"
    fi

    imageDir=$instrumentTop/images
    if test ! -d "$imageDir"; then
      echo "$funcName: INFO: no images in $instrumentTop"
      exit;
    fi

    galleryListFile=$instrumentTop/gallery-list.txt
    rm -f $galleryListFile

    galleryDir=$instrumentTop/gallery
    if test ! -d "$galleryDir"; then
      # This directory has no gallery; just exit here.
      echo "$funcName: WARNING: no gallery files with images in $imageDir"
      exit;
    fi

    cd $imageDir
    check $? "$funcName: unable to cd to image directory $imageDir"

    tmpImageList=$instrumentTop/tmpImageList.txt
    # Temporary file to list just the thumbnail images.
    tmpThumbnailList=$instrumentTop/tmpThumbnailList.txt

    # Go one sub-shell deeper to ensure this tmp file gets cleaned up.
    export tmpImageList funcName imageDir galleryDir galleryListFile instrumentTop
    (
      ls > $tmpImageList 2> /dev/null
      check $? "$funcName: unable to list images in $imageDir"

      cd $galleryDir
      check $? "$funcName: unable to cd to gallery directory $galleryDir"

      for image in `cat $tmpImageList`; do
        root=`echo "$image" | sed 's:\.[^\.]*$::'`
        check $? "$funcName: unable to determine base name of gallery images for $image"

        # Sort matching gallery file names by size so the thumbnail is listed first.
        # Hope there is one thumbnail and one gallery image matching each file name.
        galleryFiles=`ls -Sr $root* 2> /dev/null | tr '\012' ' '`
        check $? "$funcName: unable to find gallery images for $image"

        if test `echo $galleryFiles | wc -w` -eq 2; then
          echo "$image $galleryFiles" >> $galleryListFile
          # First image in list is the name of the thumbnail, including the gallery/ subdirectory
          # prefix. Add it to the thumbnail list file.
          echo "gallery/$galleryFiles" | sed 's:[  ].*::' >> $tmpThumbnailList
        fi
      done

      if test ! -f "$galleryListFile"; then
        echo "$funcName: WARNING: did not find ANY gallery files in $galleryDir"
        echo "$funcName: please examine $instrumentTop and its subdirectories"
      elif test `wc -l $tmpImageList | sed 's: .*::'` -ne `wc -l $galleryListFile | sed 's: .*::'`; then
        echo "$funcName: WARNING: did not find gallery files for every image in $imageDir"
        echo "$funcName: please examine $instrumentTop and its subdirectories"
      fi

      # This does not involve the tmp file, but it is convenient just to do this
      # here, in the sub-shell.
      cd $instrumentTop
      check $? "$funcName: unable to cd to $instrumentTop to create zip file"

      # Get rid of any previous zip file.
      rm -f gallery.zip

      if test -f $tmpThumbnailList; then
        # Zip up images in the gallery directory that match any of the files listed in the thumbnail list file
        echo "nice cat $tmpThumbnailList | zip -q gallery.zip -@"
        nice cat $tmpThumbnailList | zip -q gallery.zip -@
        check $? "$funcName: unable to zip gallery files in $galleryDir"
      fi

    )
    status=$?
    rm -f $tmpImageList
    rm -f $tmpThumbnailList
    exit $status
  )
  check $?
}

importKernelsFromMetakernel() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    metakernel=$1

    if test "$metakernel" = ""; then
      check 1 "$funcName: missing/blank first argument must specify the full path to the metakernel"
    fi

    if test ! -f "$metakernel"; then
      check 1 "$funcName: first argument $metakernel is not the path to a metakernel file"
    fi

    pathSymbol=$2

    if test "$pathSymbol" = ""; then
      check 1 "$funcName: missing/blank second argument must specify the name of the path symbol in MK"
    fi

    pathValue=$3
    if test "$pathValue" = ""; then
      check 1 "$funcName: missing/blank third argument must specify the value of the path symbol in MK"
    fi

    if test ! -d "$pathValue"; then
      check 1 "$funcName: third argument (path value) $pathValue is not the path to a directory"
    fi

    mkFileName=`echo $metakernel | sed 's:.*/::'`
    outKernelPath=`echo $metakernel | sed "s:/$mkFileName$::"`

    # Get rid of single and/or double quotes, leading/trailing space.
    # Also look for and remove a leading path symbol.
   for file in `cat $metakernel | tr -d "'" | tr -d '"' | sed 's:^[ 	]*::' | \
       sed -n 's:^$'"$pathSymbol::p" | sed 's:[ 	]*$::'`; do
     outFile="$outKernelPath$file"

     createParentDir $outFile
     check $?

     doRsync "$pathValue$file" "$outFile" "$4"
     check $?
   done
  )
  check $?
}

# Create INFO files from a SPICE metakernel plus a CSV file containing a list of images with time stamps.
# Note that infoDir in this function is the FULL path to the output INFO file directory.
createInfoFilesFromImageTimeStamps() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    metakernel=$1
    body=$2
    bodyFrame=$3
    spacecraft=$4
    instrument=$5
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
      check 1 "$funcName: blank/missing infoDir argument must be the FULL path to the output info file direoctory."
    fi

    parentDir=$(getDirPath "$infoDir/..")

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

    createDir "$infoDir"

    #  1. metakernel - full path to a SPICE meta-kernel file containing the paths to the kernel files
    #  2. body - IAU name of the target body, all caps
    #  3. bodyFrame - Typically IAU_<body>, but could be something like RYUGU_FIXED
    #  4. spacecraft - SPICE spacecraft name
    #  5. instrument - SPICE instrument name
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

    echo nice $createInfoFilesDir/createInfoFiles $metakernel $body $bodyFrame $spacecraft $instrument \
      $imageTimeStampFile "$infoDir" $imageListFile $imageListFullPathFile $missingInfoList

    nice $createInfoFilesDir/createInfoFiles $metakernel $body $bodyFrame $spacecraft $instrument \
      $imageTimeStampFile "$infoDir" $imageListFile $imageListFullPathFile $missingInfoList > \
      $logTop/createInfoFiles-$instrument.txt 2>&1
    check $? "$funcName: creating info files failed. See log file $logTop/createInfoFiles-$instrument.txt"
  )
  check $?
}

# Create INFO files from a SPICE metakernel plus a directory with FITS images that have time stamps associated with a keyword.
# Note that infoDir in this function is now the ABSOLUTE path to the output INFO file directory.
createInfoFilesFromFITSImages() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    metakernel=$1
    body=$2
    bodyFrame=$3
    spacecraft=$4
    instrument=$5
    timeStampKeyword=$6
    topDir=$7
    imageDir=$8
    infoDir="$9"
    # Proof that Bourne shell is evil (yet we love it). $10 = "$1"0. Need the curly brace if ever again
    # there is a 10th argument.

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

    if test "$instrument" = ""; then
      check 1 "$funcName: missing/blank fifth argument must specify instrument name (cosmetic but with no spaces)"
    fi

    if test "$timeStampKeyword" = ""; then
      check 1 "$funcName: missing/blank seventh argument must specify keyword used to extract time stamps"
    fi

    if test "$topDir" = ""; then
      check 1 "$funcName: missing/blank eighth argument must specify the top of the installation"
    fi

    if test "$imageDir" = ""; then
      check 1 "$funcName: missing/blank ninth argument must specify image directory relative to $topDir"
    fi

    if test ! -d "$topDir/$imageDir"; then
      check 1 "$funcName: ninth argument $imageDir must specify image directory relative to $topDir"
    fi

    if test "$infoDir" = ""; then
      check 1 "$funcName: missing/blank tenth argument must specify the absolute path to the output INFO file directory"
    fi

    # Generate image list with time stamps from the content of the image directory.
    imageTimeStampDir=$(getDirPath "$topDir/$imageDir/..")

    imageTimeStampFile="$imageTimeStampDir/imagelist-with-time.txt"

    if test ! -f $imageTimeStampFile; then
      echo extractFITSFileTimes $timeStampKeyword $topDir "$topDir/$imageDir" $imageTimeStampFile
      extractFITSFileTimes $timeStampKeyword $topDir "$topDir/$imageDir" $imageTimeStampFile
    else
      echo "$funcName: file $imageTimeStampFile already exists -- skipping extracting times from FITS images"
    fi

    createInfoFilesFromImageTimeStamps $metakernel $body $bodyFrame $spacecraft $instrument \
      $imageTimeStampFile $infoDir
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
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    imagerDir=$1
    logFile=$2

    if test "$imagerDir" = ""; then
      check 1 "$funcName: missing the directory to check"
    elif test ! -d "$imagerDir"; then
      check 1 "$funcName: argument with directory to check is not a directory: $imagerDir"
    fi

    if test ! -f "$imagerDir/make_sumfiles.in"; then
      check 1 "$funcName: cannot find file $imagerDir/make_sumfiles.in"
    fi

    tmpFileList=$funcName-$(basename "$imagerDir").tmp
    ext=`guessFileExtension $imagerDir/sumfiles`
    sed "s: .*:.$ext:" "$imagerDir/make_sumfiles.in" > $tmpFileList
    check $? "$funcName: could not edit $imagerDir/make_sumfiles.in to create $tmpFileList"

    echo "$funcName: in directory $imagerDir, comparing content of sumfiles/ directory to list in make_sumfiles.in"
    if test "$logFile" != ""; then
      echo "$funcName: in directory $imagerDir, comparing content of sumfiles/ directory to list in make_sumfiles.in" > $logFile
      checkFileList "$tmpFileList" "$imagerDir/sumfiles" >> $logFile 2>&1
      check $? "$funcName: problems with content of $imagerDir. Files checked listed in $tmpFileList. See details in $logFile"
    else
      checkFileList "$tmpFileList" "$imagerDir/sumfiles"
      check $? "$funcName: problems with content of $imagerDir. Files checked listed in $tmpFileList."
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

# Adapted from a similar function in sbmt->pipeline->rawdata->bennu->modelRawdata2processed-bennu.sh.
# This generates the imagelist-sum.txt and imagelist-fullpath-sum.txt needed for database and fixed-list queries
# starting from an input make_sumfiles.in file. The output files are placed parallel to the input file
# in the imager directory.
#
# @param imagerDir full path to directory containing input file make_sumfiles.in; also this is the location of
#        the output files.
# @param prefix the partial path prefix used in the output imagelist-fullpath-sum to create the full path
#        relative to the top of the server directory.
processMakeSumFiles() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    imagerDir="$1"
    makeSumFiles="$imagerDir/make_sumfiles.in"

    if test "$imagerDir" = ""; then
      check 1 "$funcName: missing first argument, which is the full path to where the instrument files are located"
    elif test ! -d "$imagerDir"; then
      check 1 "$funcName: first argument $imagerDir does not identify an imager directory"
    elif test ! -f "$makeSumFiles"; then
      check 1 "$funcName: input file $imagerDir/make_sumfiles.in does not exist"
    fi

    # Make sure the prefix starts and ends with a single slash:
    prefix=`echo $2 | sed 's:^/*:/:' | sed 's:/*$:/:'`
    if test "$prefix" = ""; then
      check 1 "$funcName: missing second argument, which is the partial path prefix to be written to the output file"
    fi

    rm -f $imagerDir/imagelist-sum.txt $imagerDir/imagelist-fullpath-sum.txt

    # Create imagelist-fullpath-sum.txt.
    cat $makeSumFiles | sed -e 's:.*[ 	]::' | sed "s:^:$prefix:" > $imagerDir/imagelist-fullpath-sum.txt
    check $? "$funcName: unable to create $imagerDir/imagelist-fullpath-sum.txt"

    # Create imagelist-sum.txt.
    for sumFile in `sed 's: .*::' $makeSumFiles`; do
      if test "$sumFile" = ""; then
        check 1 "$funcName: unable to determine sum file names from $makeSumFiles"
      fi

      # Find the line that begins with this sum file base name.
      # Strip out the sum file base name, then get rid of anything up to a final space.
      # What remains should be the image name.
      imageFile=`sed -n "s:^$sumFile ::p" $makeSumFiles | sed 's:.* ::'`
      if test "$imageFile" = ""; then
        check 1 "$funcName: unable to determine the image file that goes with $sumFile in $makeSumFiles"
      fi

      # Assume actual file name ends in .SUM if there is no explicit extension.
      if test `echo $sumFile | grep -c '\..*$'` -eq 0; then
        sumFile="${sumFile}.SUM"
      fi

      # Read the time stamp from the sumfile.
      timeStamp=`head -2 $imagerDir/sumfiles/$sumFile | tail -1 | \
        sed 's:^  *::' | sed 's:  *$::' | \
        sed 's:jan:01:i' | \
        sed 's:feb:02:i' | \
        sed 's:mar:03:i' | \
        sed 's:apr:04:i' | \
        sed 's:may:05:i' | \
        sed 's:jun:06:i' | \
        sed 's:jul:07:i' | \
        sed 's:aug:08:i' | \
        sed 's:sep:09:i' | \
        sed 's:oct:10:i' | \
        sed 's:nov:11:i' | \
        sed 's:dec:12:i' | \
        sed 's:  *:-:' | \
        sed 's:  *:-:' | \
        sed 's:  *:T:' | \
        sed 's:  *:-:g'`
      echo "$imageFile $timeStamp" >> $imagerDir/imagelist-sum.txt
    done
    check $? "$funcName: unable to create $imagerDir/imagelist-sum.txt"
  )
  check $?
}

# Generate a full set of model metadata files using the client distribution associated with this
# delivery.
#
# param destDir the destination directory under which the metadata files will be created
generateModelMetadata() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    destDir=$1
    logFile=$logTop/ModelMetadataGenerator.txt

    if test "$destDir" = ""; then
      check 1 "$funcName: first argument must be target area where to write model metadata."
    fi

    createDir $logTop

    echo "$sbmtCodeTop/sbmt/bin/ModelMetadataGenerator.sh $destDir"
    $sbmtCodeTop/sbmt/bin/ModelMetadataGenerator.sh $destDir >> $logFile 2>&1
    check $? "$funcName: problems generating proprietary model metadata. For details, see file $logFile"

    echo "$sbmtCodeTop/sbmt/bin/ModelMetadataGenerator.sh -pub $destDir"
    $sbmtCodeTop/sbmt/bin/ModelMetadataGenerator.sh $destDir -pub >> $logFile 2>&1
    check $? "$funcName: problems generating published model metadata. For details, see file $logFile"

  )
  check $?
}

# This takes one argument, which gives the flavor of metadata (proprietary or public).
deployModelMetadata() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    flavor=$1
    if test "$flavor" = ""; then
      check 1 "$funcName: (missing) first argument must be flavor of metadata to deploy (proprietary or published)."
    fi

    srcTop="$processedTop/$flavor/$modelMetadataDir"

    if test -d $srcTop; then
      destTop="$serverTop/$flavor/$modelMetadataDir-$processingId"

      copyDir .

      updateRelativeLink $destTop $serverTop/$flavor/$modelMetadataDir $processingId
    else
      echo "$funcName: did not find $flavor metadata to deploy under the directory $processedTop"
    fi
  )
  check $?
}

# Infer symbolic link names for arbitrary file names. This is so that the provider can use
# whatever names they want for, say, shape files, but the tool will see shape0, shape1, etc.
createFileSymLinks() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    dir=$1
    prefix=$2

    if test "$dir" = ""; then
      check 1 "$funcName: first argument missing; must be directory in which to create symbolic links"
    fi

    if test ! -d $dir; then
      check 1 "$funcName: $dDir is not a directory"
    fi

    if test "$prefix" = ""; then
      check 1 "$funcName: second argument missing; must be prefix for symbolic link names"
    fi

    cd $dir
    check $? "$funcName: unable to cd $dir"

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
        check $? "$funcName: unable to create symbolic link from $file to $linkName"

        let res=res+1
      fi
    done
  )
  check $?
}

# Unpack any archives found in the provided directory. If none, just skip.
unpackArchives() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    srcDir=$1

    if test "$srcDir" = ""; then
      check 1 "$funcName: missing/blank first argument must be source directory that may contain archive files"
    fi

    if test ! -d "$srcDir"; then
      check 1 "$funcName: first argument $srcDir does not specify a source directory that may contain archive files"
    fi

    cd $srcDir
    check $? "$funcName: cannot cd $srcDir"

    # Use . to ensure for loop has at least one match.
    for file in `ls *.tar 2> /dev/null` .; do
      if test "$file" != .; then
        echo nice tar xf $file
        nice tar xf $file
        check $? "$funcName: unable to untar file $file"
      fi
    done

    # Use . to ensure for loop has at least one match.
    for file in `ls *.tgz *.tar.gz 2> /dev/null` .; do
      if test "$file" != .; then
        echo nice tar zxf $file
        nice tar zxf $file
        check $? "$funcName: unable to untar gzipped file $file"
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
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    srcDir=$1

    regEx=$2

    if test "$srcDir" = ""; then
      check 1 "$funcName: missing/blank first argument must be source directory that may contain metakernel files"
    fi

    if test ! -d "$srcDir"; then
      check 1 "$funcName: first argument $srcDir does not specify a source directory that may contain metakernel files"
    fi

    if test "$regEx" = ""; then
      check 1 "$funcName: missing/blank second argument must be regular expression to match delivered paths in metakernels"
    fi

    cd $srcDir
    check $? "$funcName: cannot cd $srcDir"

    # Use . to ensure for loop has at least one match.
    for file in `ls *.mk *.tm 2> /dev/null` .; do
      if test "$file" != .; then
        if test ! -f "$file.bak"; then
          nice sed -ibak -e "s:$regEx:$tmpSpiceDir:" $file
          check $? "$funcName: unable to edit file $file"
        else
          echo "$funcName: $file.bak already exists -- not re-editing metakernel file $srcDir/$file"
        fi
      fi
    done

  )
  check $?
}

# Run the database generator to create a table for a particular instrument.
#
# param modelId the model identifier
# param instrument the instrument identifier, as it is or will be referred to in a Java Instrument object
# param pointing the type of pointing, usually either GASKELL or SPICE
generateDatabaseTable() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    modelId=$1
    bodyId=$2
    instrument=$3
    pointing=$4

    if test "$modelId" = ""; then
      check 1 "$funcName: missing/blank first argument, which must be the name of a model"
    fi

    if test "$bodyId" = ""; then
      check 1 "$funcName: missing/blank second argument, which must be the name of a body"
    fi

    if test "$instrument" = ""; then
      check 1 "$funcName: missing/blank third argument, which must be the name of an instrument"
    fi

    if test "$pointing" = ""; then
      check 1 "$funcName: missing/blank fourth argument, which must be the pointing type"
    fi

    tool=DatabaseGeneratorSql.sh
    pathToTool=$sbmtCodeTop/sbmt/bin/$tool

    # Just in case, make sure pointing is all uppercase.
    pointing=${pointing^^}

    logFile=$logTop/$tool-$instrument-$pointing.txt

    createDir $logTop

    echo nice $pathToTool --root-url file://$processedTop --body "${bodyId^^}" --author "$modelId" --instrument "$instrument" $pointing | \
      tee -ai $logFile
    nice $pathToTool --root-url file://$processedTop --body "${bodyId^^}" --author "$modelId" --instrument "$instrument" $pointing \
      >> $logFile 2>&1
    check $? "$funcName: $tool had an error. See log file $logFile"

  )
  check $?
}

# Create a link from one processed area to another on the same disk. This was (and
# could be again) used when one scientist delivery has been split into multiple redmine
# issues that need to be processed in sequence into the a single output area.
# Really this provides no value or new functionality beyond what createRelativeLink
# already does, and it may eventually be phased out in favor of other utilities.
linkToProcessedArea() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    target=$1

    if test "$target" = ""; then
      check 1 "$funcName: mising first argument, which must be the full path to the processed model to update"
    fi

    target=`realpath -e $target`
    check $? "$funcName: realpath cannot determine path of $target (must exist)"

    if test `echo $target | grep -c ^$pipelineProcessed` -eq 0; then
      check $? "$funcName: only can link to a model that is located under the path $pipelineProcessed"
    fi

    linkName=$2

    if test "$linkName" = ""; then
      check 1 "$funcName: missing second argument, which must be the link name"
    fi

    if test -e "$linkName"; then
      linkedArea=`realpath $linkName`
      if test "$linkedArea" != "$target"; then
        check 1 "$funcName: second argument $linkName may not exist when linking to a processed model"
      fi
    fi

    createRelativeLink $target $linkName
  )
  check $?
}

# Make the destination directory look exactly like the source directory. If destination
# and source are on the same partition, the destination will first be deleted, then
# re-created using hard links. If the destination and source are on different partitions,
# rsync is used to copy the directory.
#
# The parent directory of dest is created before the final sync, so even if this function
# fails to execute, it may have succeeded in creating the parent.
#
# @param src the source directory (must exist)
# @param dest the destination directory
#
# Errors will be thrown if any of the following occur: src or dest are missing/blank,
# src does not identify an existing directory, dest identifies a file, dest is a subdirectory
# of src.
syncDir() {
  (
    funcName=${FUNCNAME[0]}

    checkSkip $funcName "$*"

    if test "$1" = ""; then
      check 1 "$funcName: missing first argument, which must be the full path to source directory"
    fi

    if test "$2" = ""; then
      check 1 "$funcName: missing second argument, which must be the full path to destination directory"
    fi

    src="$1"
    dest="$2"

    if test ! -d "$src"; then
      check 1 "$funcName: source path is not a directory: $src"
    fi

    if test -f "$dest"; then
      check 1 "$funcName: destination path is a file: $dest"
    fi

    destParent=$(dirname "$dest")"/.." 2> /dev/null
    createDir "$destParent"

    srcPart=`df "$src" | tail -1 | sed 's: .*::'`
    destPart=`df "$destParent" | tail -1 | sed 's: .*::'`
    if test "$srcPart" = "$destPart"; then
      # Partitions are the same. Check to make sure dest does not include source.
      # Use realpath to thwart any redirections/logical path discrepancies.
      realSrc=`realpath "$src"`
      check $? "$funcName: realpath $src failed"
      realDest=`realpath "$dest"`
      check $? "$funcName: realpath $dest failed"
      if test `echo "$realDest" | grep -c "^$realSrc"` -gt 0; then
        check 1 "$funcName: destination $dest is a subdirectory of source $src"
      fi

      rm -rf $dest
      check $? "$funcName: unable to remove destination prior to sync: $dest"

      echo cp -al "$src" "$dest"
      cp -al "$src" "$dest"
      check $? "$funcName: unable to hard link files in source $src to destination $dest"
    else
      doRsyncDir "$src" "$dest" "--delete --links --copy-unsafe-links --keep-dirlinks"
    fi
  )
  check $?
}
