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

scp build/dist/internal/linux64/sbmt-${TODAYSDATE}-linux-x64.zip                    ${TARGET}/internal/releases/
scp build/dist/internal/mac64/sbmt-${TODAYSDATE}-macosx-x64.zip                     ${TARGET}/internal/releases/
scp build/dist/internal/win64-with-jre/sbmt-${TODAYSDATE}-windows-x64-with-java.zip ${TARGET}/internal/releases/
scp build/dist/internal/win64/sbmt-${TODAYSDATE}-windows-x64.zip                    ${TARGET}/internal/releases/

scp build/dist/public/linux64/sbmt-${TODAYSDATE}-linux-x64.zip                     ${TARGET}/releases/
scp build/dist/public/mac64/sbmt-${TODAYSDATE}-macosx-x64.zip                      ${TARGET}/releases/
scp build/dist/public/win64-with-jre/sbmt-${TODAYSDATE}-windows-x64-with-java.zip  ${TARGET}/releases/
scp build/dist/public/win64/sbmt-${TODAYSDATE}-windows-x64.zip                     ${TARGET}/releases/

(
cd misc/server/sbmt/internal
rm -rf output
mkdir output
nanoc
sed -i "" "s/VERSIONXXXXXX/${TODAYSDATE}/g" output/index.html output/installation.html
scp -r output/* ${TARGET}/internal/
)

(
cd misc/server/sbmt/public
rm -rf output
mkdir output
nanoc
sed -i "" "s/VERSIONXXXXXX/${TODAYSDATE}/g" output/index.html output/installation.html
scp -r output/* ${TARGET}/
)

ssh ${TARGETHOST} rm -f ${TARGETDIR}/internal/releases/sbmt-latest-linux-x64.zip
ssh ${TARGETHOST} "cd ${TARGETDIR}/internal/releases; ln -s sbmt-${TODAYSDATE}-linux-x64.zip sbmt-latest-linux-x64.zip"
ssh ${TARGETHOST} rm -f ${TARGETDIR}/internal/releases/sbmt-latest-macosx-x64.zip
ssh ${TARGETHOST} "cd ${TARGETDIR}/internal/releases; ln -s sbmt-${TODAYSDATE}-macosx-x64.zip sbmt-latest-macosx-x64.zip"
