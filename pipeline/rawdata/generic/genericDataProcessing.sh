#-------------------------------------------------------------------------------
# Customizable mission/model/body-specific data processing/import script.
#-------------------------------------------------------------------------------
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

#-------------------------------------------------------------------------------
# Update this block for each delivery.
#-------------------------------------------------------------------------------

# This string identifies the processing being performed. It must be all
# lower case and contain no underscores or whitespace. It may not be left
# blank.
processingId="redmine-2107"

# This is the full path to the delivery as provided by a scientist. This may
# or may not fully comply with all SBMT guidelines for layout and naming.
deliveryTop="/project/sbmtpipeline/deliveries/didymosa/20200420/didymosA-DRA-v01A"

# This is the *relative* path to the top output directory as it will be
# defined in the raw, processed and deployed locations.
# Must be all lower case and contain no underscores or whitespace.
outputTopPath="didymosa/didymosa-dra-v01a"

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
# standard main development branches.
saavtkBranch="saavtk1dev-redmine-2107"
sbmtBranch="sbmt1dev-redmine-2107"

# Location for deployed files.
deployedTop="/project/sbmt2/sbmt/data/bodies/$outputTopPath"
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# Check out and build saavtk and sbmt. Comment these out if you don't need
# the SBMT client for this delivery. The SBMT client is used for processing
# plate colorings, and/or images.
# checkoutCode
# buildCode

#-------------------------------------------------------------------------------
# Delivery to raw data.
#-------------------------------------------------------------------------------
srcTop="$deliveryTop"
destTop="$rawDataTop/$outputTopPath"

# Copy any/all standard model files.
# copyStandardModelFiles

# Copy and tailor this for each imager. Comment out if there are no imagers.
# copyOptionalDir draco imaging/draco
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# Raw data to processed.
#-------------------------------------------------------------------------------
srcTop="$rawDataTop/$outputTopPath"
destTop="$processedTop/$outputTopPath"

# Copy any/all standard model files.
# copyStandardModelFiles

# All the individual imagers are in the imaging directory, so can get them
# all at once this way.
# copyOptionalDir imaging

# Process plate colorings.
# discoverPlateColorings

createInfoFilesFromFITSImages imaging/draco/spice/generic.mk \
  Didymos Didymos DART Draco COR_UTC \
  imaging/draco/images imaging/draco/infofiles
#-------------------------------------------------------------------------------

# Done.
