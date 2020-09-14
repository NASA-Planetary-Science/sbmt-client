#-------------------------------------------------------------------------------
# Customizable mission/model/body-specific data processing/import script.
#-------------------------------------------------------------------------------
#
# Customize this script to copy a delivery from the deliveries area to
# the "raw data" area, and to process the raw data into the "processed data"
# area. All delivery scripts must be invoked from the sbmt account.
#
# Do not invoke this script directly! Edit it as necessary to work for a
# specific data delivery, then pass the path to this script as an argument
# to runDataProcessing.sh, which will execute this script to process that
# delivery. For detailed instructions, see the comments at the beginning of
# runDataProcessing.sh.
#
# Fill out the block below to describe the delivery and what you customized
# to make it work. Then update the remainder of the script below as needed.
#-------------------------------------------------------------------------------
# Processing Info
#-------------------------------------------------------------------------------
# Developer:
# Delivery: redmine-XXXX
# Notes:
# Information specific to this delivery and/or its processing should be
# described here.
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
echo "Tailor this script first for the specific delivery being processed." >&2
exit 1

#-------------------------------------------------------------------------------
# Update this block for each delivery. All information below should be
# included in the redmine issue and/or the delivery aamanifest.txt file.
#-------------------------------------------------------------------------------

# This is the full path to the delivery as provided by a scientist. This may
# or may not fully comply with all SBMT guidelines for layout and naming.
deliveryTop="/project/sbmtpipeline/deliveries-dart/ideal_impact1-20200629-v01/didymos/SMv01A-truth"

# The identifier of the SBMT model, which should match how the model is or
# will be identified with a ShapeModelType object. For a given body, this
# uniquely identifies the model. This may not include any whitespace. If no
# items being processed are associated with a specific model, this may be set
# to an empty string but it should not be removed. This is used to process
# plate colorings and images.
modelId="ideal_impact1-20200629-v01"

# The identifier of the body as it appears in the SBMT, which should match how
# the body is or will be identified with a ShapeModelBody object. If no items
# being processed are associated with a specific body, this may be set to an
# empty string, but it should not be removed. This is used for processing
# plate colorings and images.
bodyId="Didymos"

#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# Delivery to raw data.
#-------------------------------------------------------------------------------
srcTop="$deliveryTop"
destTop="$rawDataTop/$outputTop"

# Copying the delivery to raw data should suffice for most deliveries.
# Copy all delivered files.
copyDir .

#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# Raw data to processed. Depending on what is being delivered, include,
# comment out, and/or edit the following blocks.
#-------------------------------------------------------------------------------

# Begin standard model block. This is for most model-associated items, and
# does not include image/spectra/lidar/spice related items. This block
# properly does not include a top-level copyDir command. Rather, each type
# of model-associated item has dedicated functions as shown below.
#-------------------------------------------------------------------------------
srcTop="$rawDataTop/$outputTop"
destTop="$processedTop/$outputTop"

# Generate complete set of model metadata.
# generateModelMetadata $processedTop

# Process any/all standard model files.
# processStandardModelFiles

# Process plate colorings.
# discoverPlateColorings

# processDTMs

# End standard model block.
#-------------------------------------------------------------------------------

# Begin SPICE block. This is for anything that will use SPICE pointings, either
# directly or indirectly.
#-------------------------------------------------------------------------------
# SPICE kernel processing block.
srcTop="$rawDataTop/$outputTop"
destTop="$processedTop/$outputTop"

# Copy all delivered files.
# copyDir .

# Unpack any archives that are present in the delivered files.
# unpackArchives $destTop

# End SPICE block.
#-------------------------------------------------------------------------------

# createInfoFilesFromFITSImages imaging/draco/spice/generic.mk \
#   Didymos Didymos DART Draco COR_UTC \
#   imaging/draco/images imaging/draco/infofiles
#-------------------------------------------------------------------------------
