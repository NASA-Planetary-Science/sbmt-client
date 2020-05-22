#-------------------------------------------------------------------------------
# Customizable mission/model/body-specific data processing/import script.
#-------------------------------------------------------------------------------
#
# Customize this script to copy a delivery from the deliveries area to
# the "raw data" area, and to process the raw data into the "processed data"
# area. Run this script from a normal user account.
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
echo "Tailor this script first for the specific delivery being processed."
exit 1

#-------------------------------------------------------------------------------
# Update this block for each delivery.
#-------------------------------------------------------------------------------

# This is the full path to the delivery as provided by a scientist. This may
# or may not fully comply with all SBMT guidelines for layout and naming.
deliveryTop="/project/sbmtpipeline/deliveries/didymosa/20200420/didymosA-DRA-v01A"

# The identifier of the SBMT model. For a given body, this uniquely identifies
# the model. This may not include any whitespace. If no items being imported
# are associated with a specific model, this may be set to an empty string,
# but it should not be removed. This is used for processing plate colorings.
modelId="DidymosA-DRA-v01A"

# The identifier of the body as it appears in the SBMT. If no items being
# imported are associated with a specific body, this may be set to an
# empty string, but it should not be removed.
# This is used for processing plate colorings.
bodyId="65803 Didymos"

# Code branches used for SAAVTK/SBMT checkout. Many deliveries will work with
# any recent version of these packages, so this frequently can just be the
# standard main development branches, saavtk1dev and sbmt1dev, respectively.
saavtkBranch="saavtk1dev-redmine-2107"
sbmtBranch="sbmt1dev-redmine-2107"

#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# Delivery to raw data.
#-------------------------------------------------------------------------------
srcTop="$deliveryTop"
destTop="$rawDataTop/$outputTop"

# Copy any/all standard model files.
copyStandardModelFiles

# Copy and tailor this for each imager. Comment out if there are no imagers.
copyOptionalDir draco imaging/draco
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# Raw data to processed.
#-------------------------------------------------------------------------------
srcTop="$rawDataTop/$outputTop"
destTop="$processedTop/$outputTop"

# Copy any/all standard model files.
copyStandardModelFiles

# All the individual imagers are in the imaging directory, so can get them
# all at once this way.
copyOptionalDir imaging

# Process plate colorings.
discoverPlateColorings

# Update/check the SPICE parameters.
createInfoFilesFromFITSImages imaging/draco/spice/generic.mk \
  Didymos Didymos DART Draco COR_UTC \
  imaging/draco/images imaging/draco/infofiles
#-------------------------------------------------------------------------------
