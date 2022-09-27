#-------------------------------------------------------------------------------
# Customizable mission/model/body-specific data deployment script.
#-------------------------------------------------------------------------------
#
# Customize this script to copy a fully processed delivery from the
# the "processed data" area to the "deployed area" and create links in
# the server area. Run this script from the sbmt account.
#
# Do not invoke this script directly! Edit it as necessary to work for a
# specific data delivery, then pass the path to this script as an argument
# to runDataProcessing.sh, which will execute this script to process that
# delivery. For detailed instructions, see the comments at the beginning of
# runDataProcessing.sh.
#
# Fill out the block below to describe the delivery and what you customized
# to make it work. Then update the remainder of the script as needed.
#-------------------------------------------------------------------------------
# Processing Info
#-------------------------------------------------------------------------------
# Developer: James Peachey
# Delivery: redmine-XXXX
# Notes:
#
#-------------------------------------------------------------------------------
# DON'T USE THIS ANYMORE! Superseded by scripts under the scripts/
# subdirectory.
check 1 "This script is deprecated; prefer the ones under scripts/."
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# Do not remove or comment out this block. It prevents direct invocation.
#-------------------------------------------------------------------------------
if test "$invokedByRunner" != true; then
  echo "This script must be invoked by runDataProcessing.sh" >&2
  exit 1
fi
#-------------------------------------------------------------------------------

# When tailoring is complete, remove or comment out the next two lines
# before invoking using runDataProcessing.sh to actually process the
# delivery.
check 1 "Tailor this script first for the specific delivery being processed."

#-------------------------------------------------------------------------------
# Processed data to deployed.
#-------------------------------------------------------------------------------

# The identifier of this processing run, typically "redmine-XXXX"
processingId="redmine-XXXX"

# The identifier of the output path relative to the top of the
# raw/processed/deployed/served directory. Typically this would
# identify either a body/model, mission/instrument, or mission/spice, for
# example, 'didymosa/didymosa-dra-v01a' or 'dart/draco'.
outputTop="dart/leia/ideal-impact6-ra-20201116-v01"

# Install first into processing-specific directory.
srcTop="$processedTop/$outputTop"
destTop="$deployedTop/$outputTop-$processingId"

# Copy the processed data area to the deployed area.
copyDir .

# Update the data symbolic links at the top level in the deployed area, and
# the server area.
updateRelativeLink $destTop $deployedTop/$outputTop $processingId
updateRelativeLink $destTop $serverTop/$outputTop $processingId

# Deploy proprietary and public metadata, if any.
deployModelMetadata proprietary
deployModelMetadata published
#-------------------------------------------------------------------------------
