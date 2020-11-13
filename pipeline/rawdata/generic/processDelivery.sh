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
echo "Tailor this script first for the specific delivery being processed." >&2
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

# Copy all delivered files.
copyDir .

#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# Raw data to processed.
#-------------------------------------------------------------------------------
srcTop="$rawDataTop/$outputTop"
destTop="$processedTop/$outputTop"

# Copy any/all standard model files.
copyStandardModelFiles

# Process plate colorings.
discoverPlateColorings

processDTMs

# End standard model block.
#-------------------------------------------------------------------------------

# Begin SPICE block. This is for anything that will PROCESS SPICE kernels.
# This operation is distinct from using SPICE kernels.
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

# Begin instrument data processing block.
#-------------------------------------------------------------------------------
# Begin instrument processing section, which contains a common
# header/setup section, and one or more instrument-specific sub-blocks.

# Begin common (all-instrument) header/setup
#-------------------------------------------------------------------------------
# This section includes variables that are common to all instruments in
# this delivery.
srcTop="$rawDataTop/$outputTop"
destTop="$processedTop/$outputTop"

# Common settings for USING SPICE.
#-------------------------------------------------------------------------------
# These variables are only used if this delivery generates INFO files from
# SPICE kernels. These variables are valid for all instruments in this
# delivery.
bodyId="${bodyId^^}" # usually this is same as used for coloring, but all caps.
bodyFrame="920065803_FIXED" # Didymos-specific.
#bodyFrame="120065803_FIXED" # Dimorphos-specific.

# End common (all-instrument) header/setup
#-------------------------------------------------------------------------------

# Need an Instrument sub-block like below for each instrument in this delivery.


skipSection="true" # THIS SHOULD ALWAYS BE true WHEN CHECKING THIS IN!!!

# Instrument sub-block (DRACO).
#-------------------------------------------------------------------------------
scId="DART"
instrument="draco"

# Copy all delivered instrument files.
copyDir $instrument

#-------------------------------------------------------------------------------
# Process SPICE inputs for this sub-block. This sub-section is only needed
# if this delivery requires generating INFO files from SPICE kernels
# for this instrument. Otherwise, recommend uncommenting the following line:
# skipSection="true" THIS SHOULD ALWAYS BE true WHEN CHECKING THIS IN!!!

metakernel="impact.tm" # relative to $tmpSpiceDir.
instFrame="DART_DRACO_2X2" # This is specific to DRACO.
imageDir="$outputTop/$instrument/images"
infoFileDir="$outputTop/$instrument/infofiles"
timeKeyword="COR_UTC" # This is only used if extracting times from FITS files.

# Make SPICE kernels available in the temporary SPICE directory. This is so
# that any absolute paths in the metakernel may be edited to be as short
# as possible. Note there is only one temporary directory, so cannot
# simulateously process two deliveries that use SPICE kernels.
createLink $spiceKernelTop/$instrument $tmpSpiceDir

# Generate the info files for the images from the SPICE kernels using times in
# FITS images. If the images don't have time stamps, can use
# createInfoFilesFromTimeStamps instead.
createInfoFilesFromFITSImages $metakernel \
  $bodyId $bodyFrame $scId $instrument $instFrame $timeKeyword \
  $processedTop $imageDir $infoFileDir
#-------------------------------------------------------------------------------

# Set up galleries (if present).
createGalleryList $destTop/$instrument

# Update database tables.
# This symbolic link is needed because the database generator appends "/data"
# to the root URL, but this level of directory is not used in deliveries.
createRelativeLink $processedTop $processedTop/data

generateDatabaseTable ${instrument^^} SPICE

# End Instrument sub-block (DRACO).
#-------------------------------------------------------------------------------


skipSection="true" # THIS SHOULD ALWAYS BE true WHEN CHECKING THIS IN!!!

# Instrument sub-block (LEIA).
#-------------------------------------------------------------------------------
scId="LICIA"
instrument="leia"

# Copy all delivered instrument files.
copyDir $instrument

#-------------------------------------------------------------------------------
# Process SPICE inputs for this sub-block. This sub-section is only needed
# if this delivery requires generating INFO files from SPICE kernels
# for this instrument. Otherwise, recommend uncommenting the following line:
# skipSection="true" THIS SHOULD ALWAYS BE true WHEN CHECKING THIS IN!!!

metakernel="leia.tm" # relative to $tmpSpiceDir.
instFrame="LICIA_PL-1" # THIS IS SPECIFIC TO LEIA.
imageDir="$outputTop/$instrument/images"
infoFileDir="$outputTop/$instrument/infofiles"
timeKeyword="IMG_UTC" # This is only used if extracting times from FITS files.

# Make SPICE kernels available in the temporary SPICE directory. This is so
# that any absolute paths in the metakernel may be edited to be as short
# as possible. Note there is only one temporary directory, so cannot
# simulateously process two deliveries that use SPICE kernels.
createLink $spiceKernelTop/$instrument $tmpSpiceDir

# Generate the info files for the images from the SPICE kernels using times in
# FITS images. If the images don't have time stamps, can use
# createInfoFilesFromTimeStamps instead.
createInfoFilesFromFITSImages $metakernel \
  $bodyId $bodyFrame $scId $instrument $instFrame $timeKeyword \
  $processedTop $imageDir $infoFileDir
#-------------------------------------------------------------------------------

# Set up galleries (if present).
createGalleryList $destTop/$instrument

# Update database tables.
# This symbolic link is needed because the database generator appends "/data"
# to the root URL, but this level of directory is not used in deliveries.
createRelativeLink $processedTop $processedTop/data

generateDatabaseTable ${instrument^^} SPICE

# End Instrument sub-block (LEIA).
#-------------------------------------------------------------------------------


skipSection="true" # THIS SHOULD ALWAYS BE true WHEN CHECKING THIS IN!!!

# Instrument sub-block (LUKE).
#-------------------------------------------------------------------------------
scId="LICIA"
instrument="luke"

# Copy all delivered instrument files.
copyDir $instrument

#-------------------------------------------------------------------------------
# Process SPICE inputs for this sub-block. This sub-section is only needed
# if this delivery requires generating INFO files from SPICE kernels
# for this instrument. Otherwise, recommend uncommenting the following line:
# skipSection="true" THIS SHOULD ALWAYS BE true WHEN CHECKING THIS IN!!!

metakernel="luke.tm" # relative to $tmpSpiceDir.
instFrame="LICIA_PL-2" # THIS IS SPECIFIC TO LUKE.
imageDir="$outputTop/$instrument/images"
infoFileDir="$outputTop/$instrument/infofiles"
timeKeyword="IMG_UTC" # This is only used if extracting times from FITS files.

# Make SPICE kernels available in the temporary SPICE directory. This is so
# that any absolute paths in the metakernel may be edited to be as short
# as possible. Note there is only one temporary directory, so cannot
# simulateously process two deliveries that use SPICE kernels.
createLink $spiceKernelTop/$instrument $tmpSpiceDir

# Generate the info files for the images from the SPICE kernels using times in
# FITS images. If the images don't have time stamps, can use
# createInfoFilesFromTimeStamps instead.
createInfoFilesFromFITSImages $metakernel \
  $bodyId $bodyFrame $scId $instrument $instFrame $timeKeyword \
  $processedTop $imageDir $infoFileDir
#-------------------------------------------------------------------------------

# Set up galleries (if present).
createGalleryList $destTop/$instrument

# Update database tables.
# This symbolic link is needed because the database generator appends "/data"
# to the root URL, but this level of directory is not used in deliveries.
createRelativeLink $processedTop $processedTop/data

generateDatabaseTable ${instrument^^} SPICE

# End Instrument sub-block (LUKE).
#-------------------------------------------------------------------------------


# End Instrument processing sub-blocks.
#-------------------------------------------------------------------------------
