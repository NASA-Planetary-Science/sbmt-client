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
# Delivery:
# Notes:
#
#-------------------------------------------------------------------------------

# When tailoring is complete, remove or comment out the next two lines
# before invoking using runDataProcessing.sh to actually process the
# delivery.
echo "Tailor this script first for the specific delivery being processed." >&2
exit 1

#-------------------------------------------------------------------------------
# Processed data to deployed.
#-------------------------------------------------------------------------------
# Install first into processing-specific directory.
srcTop="$processedTop/$outputTop"
destTop="$deployedTop/$outputTop/$processingId"

# Copy the entire processed area to the deployed area.
copyDir $srcTop $destTop

# Update links in the server area to point to the deployed directory.
if test "$serverTop" != ""; then
  srcTop="$destTop"
  destTop="$serverTop"
  linkStandardModelFiles
  
  # Handle imager
fi
#-------------------------------------------------------------------------------
