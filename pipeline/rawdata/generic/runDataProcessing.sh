#! /bin/sh
#-------------------------------------------------------------------------------
# Developer:      James Peachey
# Description:    Top-level generic tool for processing data deliveries
#-------------------------------------------------------------------------------
#
#-------------------------------------------------------------------------------
# Instructions
#-------------------------------------------------------------------------------
# This script functions as the "main" driver for processing a data delivery.
# The preconditions for processing a delivery are: a) a data provider has
# placed a
# set/collection of data in the "delivered" area and b) the provider has
# requested the delivery be processed and provided whatever instructions
# and metadata are necessary to the developer. The deliveryThe provider also must provide
# instructions and metadata about the delivery. normally proceeds in three stages:
#   Delivered-to-Raw-data: 
#
# The following variables will be properly set by runDataProcessing.sh
# before this script is run, so you may safely use them in this script:
#
#       rawDataTop: directory where this script is located, used as the
#                   the top of the "raw" area for this delivery.
#     processedTop: top of this corresponding processed area for this
#                   delivery. This is usually parallel to rawDataTop.
#      sbmtCodeTop: where sbmt and saavtk are checked out and built.
#
# Functions defined in dataProcessingFunctions.sh are available in this
# script. By convention, all higher-level functions defined there
# are relatively smart in that they will try to detect and skip steps
# that were completed successfully in a previous run. The functions are
# also designed such that even if a step is repeated, there will be no
# harm done other than the loss of time to repeat the step.
#
# When a function encounters an error performing a particular step,
# the error will be logged and all downstream processing that depends on
# that step being completed will be skipped, but otherwise processing will
# continue until the end of the current stage.
# 
# Functions with the word "Optional" in the name indicate that the
# function will not throw an error if the operation cannot be completed,
# for example if this delivery does not include the item in question.
#
# Otherwise, this script will exit immediately when an error is encountered
# so that the developer may correct the problem and then execute this script
# again.
#
# Copy the files runDataProcessing.sh and dataProcessingFunctions.sh into the
# same directory where this script is located, and run the copy of
# runDataProcessing.sh so that the processing scripts used are archived
# along with the rest of the delivery.
#
usage() {
  echo "Usage: $0 dataProcessingScript"
  echo ""
  echo "          dataProcessingScript is a text file containing specific commands"
  echo "              to be used for this processing action."
}

if test $# -ne 1; then
  (usage >&2)
  exit 1
elif test "$1" = "-h"; then
  usage
  exit 0
fi

processingScript=$1

if test ! -f $processingScript; then
  echo "Script $processingScript is not a file" >&2
  exit 1
fi

thisDir=$(dirname "$0")
commonFunctions=$thisDir/dataProcessingFunctions.sh

if test ! -f $commonFunctions; then
  echo "Common functions file not found: $commonFunctions" >&2
  exit 1
fi

. $commonFunctions

# Guess the root of the raw data hierarchy (the directory containing rawdata/).
# Locate it based on the processing script location.
# This is probably several levels up from the preocessing script.
rawDataRootDir=$(guessRawDataParentDir "$processingScript")

# Top of the raw data is just the directory containing the processing script.
rawDataTop=$(getDirName "$processingScript")

# Processing path is the part of rawDataTop that is below rawDataRootDir/rawdata.
processingPath=`echo $rawDataTop | sed "s:$rawDataRootDir/[^/][^/]*/*::"`

# Processed directory is parallel to raw data directory.
processedTop="$rawDataRootDir/processed/$processingPath"

sbmtCodeTop=$rawDataTop

# Environment variables:
export SAAVTKROOT="$sbmtCodeTop/saavtk"
export SBMTROOT="$sbmtCodeTop/sbmt"
export PATH="$PATH:/project/sbmtpipeline/software/heasoft/bin"

echo "rawDataTop is $rawDataTop"
echo "processedTop is $processedTop"
echo "sbmtCodeTop is $sbmtCodeTop"
echo "HEASoft/Ftools installation is in /project/sbmtpipeline/software/heasoft/bin"

. "$rawDataTop/$(basename $processingScript)"
