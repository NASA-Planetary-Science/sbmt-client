#!/bin/bash

# Run from top level folder of git repo to deploy SBMT to server

set -e
set -o pipefail

DIR=`dirname "$0"`


TODAYSDATE=`date "+%Y.%m.%d"`

TARGETHOST=mirage
TARGETDIR=/disks/d0180/htdocs-sbmt
# Uncomment to test in staging area
#TARGETDIR=$TARGETDIR/internal/stage
TARGET="${TARGETHOST}:${TARGETDIR}"

scp build/sbmt-extras-${TODAYSDATE}-macosx-x64.zip ${TARGET}/internal/tools/releases/

(
cd misc/server/tools
pandoc -t html -s index.md -o index.html
sed -i "" "s/VERSIONXXXXXX/${TODAYSDATE}/g" index.html
scp index.html ${TARGET}/internal/tools/
)
