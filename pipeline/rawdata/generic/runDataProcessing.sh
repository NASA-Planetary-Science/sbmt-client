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
# placed a set/collection of data in the "deliveries" area and b) the provider
# has provided whatever instructions and metadata are necessary to the
# developer, and requested the delivery be processed, typically using a
# redmine issue.
#
# This script in itself just imports a library of commonly-needed shell script
# functions, sets up the runtime environment and then invokes the "payload",
# a data processing script provided as one of the arguments. Presumably the
# payload will perform one or more of the steps needed to process the data
# from deliveries -> raw data -> processed -> deployed -> served.
#
# The following variables will be properly set by runDataProcessing.sh
# before the payload script is run, so you may safely use these in the
# payload script:
#
#        scriptName: the name of the script currently being run; intended for
#                    use in output text, or for log file names, etc.
#
#       sbmtCodeTop: where sbmt and saavtk are checked out and built.
#
#            logTop: absolute path to the location under raw data where
#                    log files from individual processing steps may be
#                    located.
#
#   pipelineRawData: general top of the raw data area, under which specific
#                    deliveries are imported, processed and deployed.
#
#        rawDataTop: directory where the payload script is located; assumed to
#                    be the top of the "raw" area for this specific delivery.
#
# pipelineProcessed: general top level of the processed area, under which
#                    specific processed deliveries are staged for deploymeent.
#
#      processedTop: top of this corresponding processed area for this
#                    specific delivery. This is usually parallel to rawDataTop.
#
#       deployedTop: general top of the corresponding deployed data area for
#                    this delivery, the final location under which specific
#                    delivered data is actually placed.
#
#         serverTop: the top of the server area, in which a symbolic link
#                    to the deployed directory will be created during
#                    deployment. Normally this is the test server.
#
#  modelMetadataDir: directory name under which generated model metadata files
#                    are located.
#
#       tmpSpiceDir: directory name under which spice files are temporarily
#                    unpacked during processing.
#
# Functions defined in the common functions library dataProcessingFunctions.sh
# are available in the payload script. These functions will try when possible to
# avoid repeating expensive operations that were completed successfully
# in a previous run. The functions are also designed such that even if an
# operation is repeated, there will be no harm done other than the loss of time
# to repeat the step. Thus it is generally ok to invoke a payload script more
# than once.
#
# When a function encounters an error performing a particular step,
# the error will be logged and the invoking shell will exit.
#
# Functions with the word "Optional" in the name indicate that the
# function will not throw an error if inputs needed for the operation are not
# included in the delivery being processed. They will still throw errors
# if the optional step is attempted but a problem is encountered.
#
# In addition, the runtime environment is set up to include the HEASoft/Ftools
# package, which has a rich, (albeit quirky) set of utilities for manipulating
# FITS files.
#
# Copy the files runDataProcessing.sh and dataProcessingFunctions.sh into the
# same directory where this script is located, and run that copy of
# runDataProcessing.sh so that the processing framework used is archived
# along with the rest of the delivery.
#
runnerScript=`echo $0 | sed 's:.*/::'`
# Date stamp to be used in outputs.
dateStamp=`date '+%Y-%m-%dT%H%M%S'`

usage() {
  echo "--------------------------------------------------------------------------------"
  echo "Usage: $runnerScript processDeliveryScript processingID outputTop"
  echo ""
  echo "       processDeliveryScript is a Bourne shell script containing specific"
  echo "           commands to be used for this processing action, typically a locally"
  echo "           tailored processing or deployment script."
  echo ""
  echo "        processingID identifies the processing run, typically 'redmine-XXXX'"
  echo ""
  echo "        outputTop identifies the output path relative to the top of"
  echo "            the raw/processed/deployed/served directory. Typically this would"
  echo "            identify either a body/model or mission/instrument, for"
  echo "            example, 'didymosa/didymosa-dra-v01a' or 'dart/draco'."
  echo ""
  echo "        Read the top block of $runnerScript for further details about how"
  echo "        it works."
  echo "--------------------------------------------------------------------------------"
}

if test "$1" = "-h"; then
  usage
  exit 0
elif test $# -lt 3; then
  (usage >&2)
  exit 1
fi

echo
echo "********************************************************************************"
echo "$dateStamp: starting to run $runnerScript"
echo "********************************************************************************"

thisDir=$(dirname "$0")
commonFunctions=$thisDir/dataProcessingFunctions.sh

if test ! -f $commonFunctions; then
  echo "Common functions file not found: $commonFunctions" >&2
  exit 1
fi

. $commonFunctions

confirmSbmt "$runnerScript: you must be logged into the sbmt account to process deliveries"

processingScript=$1
processingId=$2
outputTop=$3

if test ! -f $processingScript; then
  (usage >&2)
  check 1 "Script $processingScript is not a file"
fi

# Extract the name of the processing script, which may be used in
# output messages etc.
scriptName=`echo $processingScript | sed 's:.*/::'`

#-------------------------------------------------------------------------------
# Assume the absolute path to the processing script is:
# /project/sbmtpipeline/rawdata/arbitrary/path/to/model/or/instrument/redmine-1234/processDelivery.sh
# The next block of code extracts these variables:
#     pipelineTop = /project/sbmtpipeline
#     rawDataTop = /project/sbmtpipeline/rawdata/arbitrary/path/to/model/or/instrument/redmine-1234
#     processingPath = arbitrary/path/to/model/or/instrument/redmine-1234
# Guess the root of the raw data hierarchy (the directory containing rawdata/).
# Locate it based on the processing script location.
# This is probably several levels up from the supplied payload script.
# Interpret this to be the location of the pipeline as a whole.
pipelineTop=$(guessRawDataParentDir "$processingScript")

# Top of the raw data is just the directory containing the processing script.
rawDataTop=$(getDirPath "$processingScript")

# Processing path is the part of rawDataTop that is below pipelineTop/rawdata.
processingPath=`echo $rawDataTop | sed "s:$pipelineTop/[^/][^/]*/*::"`
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# Pipeline's raw data area; ancestor of this delivery, presumably all deliveries.
pipelineRawData=`echo $rawDataTop | sed "s:/$processingPath$::"`

# Pipeline's processed area; "processed" subdirectory of pipeline top.
pipelineProcessed="$pipelineTop/processed"

# Processed directory is parallel to raw data directory.
processedTop="$pipelineProcessed/$processingPath"

# Location for deployed files.
deployedTop="/project/sbmt2/sbmt/data/bodies"

# Location of the server top.
serverTop="/project/sbmt2/test"

# Location of the code.
sbmtCodeTop=$rawDataTop

# Location of log files.
logTop=$rawDataTop/logs/$dateStamp

# Bodies metadata directory name. Must be kept in sync with BodyViewConfig.getConfigInfoVersion().
modelMetadataDir=allBodies-9.0

# Directory in which to unpack SPICE files. Should be as short as possible
# due to SPICE path restrictions.
tmpSpiceDir="/project/sbmt2/spice"

# Environment variables:
export SAAVTKROOT="$sbmtCodeTop/saavtk"
export SBMTROOT="$sbmtCodeTop/sbmt"
export PATH="$PATH:/project/sbmtpipeline/software/heasoft/bin"

echo "--------------------------------------------------------------------------------"
echo "Variable settings:"
echo "--------------------------------------------------------------------------------"
echo "processingId is $processingId"
echo "outputTop is $outputTop"
echo "scriptName is $scriptName"
echo "sbmtCodeTop is $sbmtCodeTop"
echo "logTop is $logTop"
echo "pipelineRawData is $pipelineRawData"
echo "rawDataTop is $rawDataTop"
echo "pipelineProcessed is $pipelineProcessed"
echo "processedTop is $processedTop"
echo "deployedTop is $deployedTop"
echo "serverTop is $serverTop"
echo "modelMetadataDir is $modelMetadataDir"
echo "tmpSpiceDir is $tmpSpiceDir"
echo "HEASoft/Ftools installation is in /project/sbmtpipeline/software/heasoft/bin"
echo "--------------------------------------------------------------------------------"
echo "Executing $processingScript"
echo "--------------------------------------------------------------------------------"

# This variable is set so that scripts can tell whether they were run by this runner
# script, as opposed to being run directly.
invokedByRunner=true

. "$rawDataTop/$(basename $processingScript)"

echo "--------------------------------------------------------------------------------"
echo "Done with processing that started at $dateStamp"
echo "Logs from this run are in $logTop"
date '+%Y-%m-%dT%H%M%S'
echo "--------------------------------------------------------------------------------"
echo