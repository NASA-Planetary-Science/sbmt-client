#! /bin/sh
#-------------------------------------------------------------------------------
# This script checks out and build saavtk and sbmt in the current directory.
# The command line arguments identify the code branches to use. The main use
# case for this script is setting up client code to process a model delivery,
# but this script should work to create a client in any command-line context.
#
# To see the command line options, run the script with no arguments, or read
# the "usage" code below. Some other options require editing the script. These
# are described in a section below.
#
# This script is *mostly* self-contained, using only Unix commands. The one
# exception is that it uses the data-permissions.pl tool to set the
# permissions of the checkout directory to be group-writable by sbmtsw.
#
# Options that require editing.
#-------------------------------------------------------------------------------
# The "mission", a.k.a. launch configuration identifies the client to build.
# By default this is TEST_APL_INTERNAL, set in a variable here, so
# to pick a different client, modify this script before running it.
# However, please note that when processing deliveries, TEST_APL_INTERNAL
# is the "universal" client, i.e., this is almost always the right client
# to use for processing, even if the delivery is geared toward a release
# of some other mission client.
mission=TEST_APL_INTERNAL
#
# To force one or more previously-completed steps to be repeated, change any/all
# of the below variables to true.
forceSaavtkCheckout=false
forceSbmtCheckout=false
forceBuild=false
#
# It is assumed that this script is being run on the server most of the time,
# and that it may be operating outside a full check-out of sbmt. It looks for
# data-permssions.pl here. If it's not found, the permissions step is just
# skipped. The permissions step is executed asynchronously and errors are
# ignored. Edit this setting to find the dataPermissions script somewhere
# else.
dataPermissions=/project/sbmt2/sbmt/scripts/data-permissions.pl
#-------------------------------------------------------------------------------
# Script begins here.
thisScript=`echo $0 | sed 's:.*/::'`

if test $# -lt 1 -o $# -gt 2; then
  echo "Usage: $thisScript branch-suffix" >&2
  echo "           or" >&2
  echo "       $thisScript saavtk-branch sbmt-branch" >&2
  echo >&2
  echo "       In the first (single-argument) form, the branch-suffix will be used" >&2
  echo "       to construct both branch names." >&2
  echo "       Example: $thisScript redmine-2177" >&2
  echo "       would check out and build branches saavtk1dev-redmine-2177 and" >&2
  echo "       sbmt1dev-redmine-2177, respectively." >&2
  echo >&2
  echo "       In the second (two-argument) form, each branch name is explicitly given" >&2
  echo "       verbatim on the command line." >&2
  echo "       Example: $thisScript saavtk1dev sbmt1dev-redmine-2177" >&2
  echo "       would check out and build branches saavtk1dev and" >&2
  echo "       sbmt1dev-redmine-2177, respectively." >&2
  echo >&2
  echo "       This script builds for the TEST_APL_INTERNAL client by default." >&2
  echo "       This script will skip completed steps if it is re-started." >&2
  echo "       Both these behaviors may be overridden by editing the script." >&2
  echo "       For details, see the comments at the top of $thisScript." >&2
  exit 1
fi

# Date stamp to be used in log files.
dateStamp=`date '+%Y-%m-%dT%H%M%S'`
logDir="logs/$dateStamp"

# Interpret command line.
if test $# -eq 1; then
  # Single-argument form: tack the suffix onto the dev branches.
  saavtkBranch="saavtk1dev-$1"
  sbmtBranch="sbmt1dev-$1"
else
  # Two-argument form: assign arguments to branch names.
  saavtkBranch=$1
  sbmtBranch=$2
fi

echo "Attempting to check out and build $mission client with branches $saavtkBranch $sbmtBranch"

rootDir=`pwd -L`
export SAAVTKROOT=$rootDir/saavtk
export SBMTROOT=$rootDir/sbmt

didSomething=false

makeLogDir() {
  if test ! -d $logDir; then
    echo "mkdir -p $logDir"
    mkdir -p $logDir
    if test $? -ne 0; then
      echo "Could not create log directory" >&2
      exit 1
    fi
  fi
}

if test "$forceSaavtkCheckout" = true; then
  echo "Forcing a fresh checkout of saavtk." 
  rm -f .git-clone-saavtk-succeeded
  rm -rf saavtk
fi

if test "$forceSbmtCheckout" = true; then
  echo "Forcing a fresh checkout of sbmt." 
  rm -f .git-clone-sbmt-succeeded
  rm -rf sbmt
fi

if test "$forceBuild" = true; then
  echo "Forcing a fresh build."
  rm -f .make-release-succeeded
  if test -d sbmt; then
    makeLogDir

    echo "(cd sbmt; nice make clean > ../$logDir/make-clean.txt 2>&1)"
    (cd sbmt; nice make clean > ../$logDir/make-clean.txt 2>&1)
    if test $? -ne 0; then
      echo "Problem with make clean prior to rebuilding. See $logDir/make-clean.txt" >&2
      exit 1
    fi
  fi
fi

# Check out saavtk unless it's already been checked out.
if test -f .git-clone-saavtk-succeeded; then
  echo "Skipping saavtk checkout steps -- looks like they were completed already."
else
  makeLogDir

  echo "nice git clone http://hardin:8080/scm/git/vtk/saavtk --branch $saavtkBranch 2>&1 | cat > $logDir/git-clone-saavtk.txt 2>&1"
  nice git clone http://hardin:8080/scm/git/vtk/saavtk --branch $saavtkBranch 2>&1 | cat > $logDir/git-clone-saavtk.txt 2>&1
  if test $? -ne 0; then
    echo "Problem with git saavtk checkout. See $logDir/git-clone-saavtk.txt" >&2
    exit 1
  fi
  didSomething=true
  touch .git-clone-saavtk-succeeded
fi

# Check out sbmt unless it's already been checked out.
if test -f .git-clone-sbmt-succeeded; then
  echo "Skipping sbmt checkout steps -- looks like they were completed already."
else
  makeLogDir

  echo "nice git clone http://hardin:8080/scm/git/sbmt --branch $sbmtBranch 2>&1 | cat > $logDir/git-clone-sbmt.txt 2>&1"
  nice git clone http://hardin:8080/scm/git/sbmt --branch $sbmtBranch 2>&1 | cat > $logDir/git-clone-sbmt.txt 2>&1
  if test $? -ne 0; then
    echo "Problem with git sbmt checkout. See $logDir/git-clone-sbmt.txt" >&2
    exit 1
  fi
  didSomething=true
  touch .git-clone-sbmt-succeeded
fi

# Build release unless this has been done.
if test -f .make-release-succeeded; then
  echo "Skipping build steps -- looks like they were completed already."
else
  makeLogDir

  echo "$SBMTROOT/misc/scripts/set-released-mission.sh $mission"
  $SBMTROOT/misc/scripts/set-released-mission.sh $mission
  if test $? -ne 0; then
    echo "Problem setting released mission" >&2
    exit 1
  fi

  echo "(cd sbmt; nice make release > ../$logDir/make-release.txt 2>&1)"
  (cd sbmt; nice make release > ../$logDir/make-release.txt 2>&1)
  if test $? -ne 0; then
    echo "Problem with make release. See $logDir/make-release.txt" >&2
    exit 1
  fi
  didSomething=true
  touch .make-release-succeeded
fi

if test -x $dataPermissions -a $didSomething = true; then
  # Spawn a silent background task to set the permissions.
  echo "Trying to fix permissions in background..."
  nice $dataPermissions $logDir saavtk sbmt > /dev/null 2>&1 &
fi

echo "$thisScript succeeded. This distribution is ready to be used. Full"
echo "instructions for processing model deliveries may be found in"
echo "sbmt/pipeline/rawdata/generic/AAREADME.txt"
