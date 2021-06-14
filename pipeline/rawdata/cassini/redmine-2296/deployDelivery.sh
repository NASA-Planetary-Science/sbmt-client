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
# Developer:
# Delivery: redmine-XXXX
# Notes:
#
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
# Install first into processing-specific directory.
srcTop="$processedTop/$outputTop"
destTop="$deployedTop/$outputTop-$processingId"

# Copy the processed data area to the deployed area.
copyDir .

# To deliver Calypso/Thomas 2018, would need to uncomment the line below while
# deploying the model in issue #2057. This would only work after #2058 is
# correctly deployed. If this works correctly, at the end there would be
# a symbolic link named iss in the directory:
# /project/sbmt2/sbmt/data/bodies/calypso/thomas-2018-redmine-2057
# The symbolic link would point to ../daly-2020/iss. That will do exactly
# what is written in redmine: future updates to daly-2020 will automatically
# be mirrored in thomas-2018 without redelivering thomas-2018.
# Note I didn't actually test this, so if it doesn't do the right thing,
# you may need to tweak it and re-run it:
# createRelativeLink $deployedTop/calypso/daly-2020/iss $destTop/iss

# Update the data symbolic links at the top level in the deployed area, and
# the server area.
updateRelativeLink $destTop $deployedTop/$outputTop $processingId
updateRelativeLink $destTop $serverTop/$outputTop $processingId

# Deploy proprietary and public metadata, if any.
deployModelMetadata proprietary
deployModelMetadata published
#-------------------------------------------------------------------------------
