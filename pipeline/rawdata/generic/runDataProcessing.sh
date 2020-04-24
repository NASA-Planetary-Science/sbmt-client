#! /bin/sh

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

echo "rawDataTop is $rawDataTop"
echo "processedTop is $processedTop"
echo "sbmtCodeTop is $sbmtCodeTop"

. "$rawDataTop/$(basename $processingScript)"
