#!/bin/bash

# Run from top level folder of git repo to deploy SBMT to server

set -e
set -o pipefail

DIR=`dirname "$0"`


TODAYSDATE=`date "+%Y.%m.%d"`

TARGETHOST=bennu
TARGETDIR=/disks/d0180/htdocs-sbmt
# Uncomment to test in staging area
#TARGETDIR=$TARGETDIR/internal/stage
TARGET="${TARGETHOST}:${TARGETDIR}"

chmod 664 build/dist/internal/linux64/sbmt-${TODAYSDATE}-linux-x64.zip 
chmod 664 build/dist/internal/mac64/sbmt-${TODAYSDATE}-macosx-x64.zip 
chmod 664 build/dist/internal/win64/sbmt-${TODAYSDATE}-windows-x64.zip 

chmod 664 build/dist/public/linux64/sbmt-${TODAYSDATE}-linux-x64.zip 
chmod 664 build/dist/public/mac64/sbmt-${TODAYSDATE}-macosx-x64.zip 
chmod 664 build/dist/public/win64/sbmt-${TODAYSDATE}-windows-x64.zip 

cp  build/dist/internal/linux64/sbmt-${TODAYSDATE}-linux-x64.zip ${TARGET}/internal/releases/
cp  build/dist/internal/mac64/sbmt-${TODAYSDATE}-macosx-x64.zip  ${TARGET}/internal/releases/
cp  build/dist/internal/win64/sbmt-${TODAYSDATE}-windows-x64.zip ${TARGET}/internal/releases/

cp  build/dist/public/linux64/sbmt-${TODAYSDATE}-linux-x64.zip   ${TARGET}/releases/
cp  build/dist/public/mac64/sbmt-${TODAYSDATE}-macosx-x64.zip    ${TARGET}/releases/
cp  build/dist/public/win64/sbmt-${TODAYSDATE}-windows-x64.zip   ${TARGET}/releases/

(
cd misc/server/sbmt/internal
rm -rf output
mkdir output
nanoc
/bin/sed -i "s/VERSIONXXXXXX/${TODAYSDATE}/g" output/index.html output/installation.html
cp ../../../../doc/userhelp/SBMT_tutorial_STM.pdf output
cp ../../../../doc/userhelp/japanese_instruction_sbmt_apl.pdf output
cp -r output/* ${TARGET}/internal/
)
(
cd misc/server/sbmt/public
rm -rf output
mkdir output
nanoc
/bin/sed -i "s/VERSIONXXXXXX/${TODAYSDATE}/g" output/index.html output/installation.html
cp -r output/* ${TARGET}/
)
