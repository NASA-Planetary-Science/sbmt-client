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
# Developer: James Peachey
# Delivery: redmine-XXXX
# Notes:
# Information specific to this delivery and/or its processing should be
# included here.
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
  check 1 "This script must be invoked by runDataProcessing.sh"
fi
#-------------------------------------------------------------------------------

# When tailoring is complete, remove or comment out the next line
# before invoking using runDataProcessing.sh to actually process the
# delivery.
check 1 "Tailor this script first for the specific delivery being processed."

#-------------------------------------------------------------------------------
# Update this block for each delivery. All information below should be
# included in the redmine issue and/or the delivery aamanifest.txt file.
# Then tailor the rest of the script to use the data in this block as needed
# to process this delivery.
#-------------------------------------------------------------------------------

# The identifier of this processing run, typically "redmine-XXXX"
processingId="redmine-XXXX"

# The identifier of the output path relative to the top of the
# raw/processed/deployed/served directory. Typically this would
# identify either a body/model, mission/instrument, or mission/spice, for
# example, 'didymosa/didymosa-dra-v01a' or 'dart/draco'.
# For a model-based delivery, this is the same as
# SmallBodyViewConfig.rootDirOnServer without the leading slash.
outputTop="didymos/ideal-impact4-ra-20210211-v01"

# This is the full path to the delivery as provided by a scientist. This may
# or may not fully comply with all SBMT guidelines for layout and naming.
deliveryTop="/project/sbmtpipeline/deliveries-dart/ideal_impact4-RA-20210211-v01/didymos-dimorphos"

# The identifier of the SBMT model, which should match how the model is or
# will be identified with a ShapeModelType object. For a given body, this
# uniquely identifies the model. This may not include any whitespace. If no
# items being processed are associated with a specific model, this may be set
# to an empty string but it should not be removed. This is used to process
# plate colorings and images. This is the same as SmallBodyViewConfig.modelId.
modelId="ideal-impact4-ra-20210211-v01"

# The identifier of the body as it appears in the SBMT, which should match how
# the body is or will be identified with a ShapeModelBody object. If no items
# being processed are associated with a specific body, this may be set to an
# empty string, but it should not be removed. This is used for processing
# plate colorings and images.
bodyId="Didymos"

# Uncomment and edit this as needed if updating an already-processed model as
# part of a delivery sequence. Because this in-effect changes files that
# have already been processed, this should only be done to update
# models that have not been deployed yet. To update a model that was already
# deployed, need to reprocess it first, then update that processed model.
# areaToUpdate="$pipelineProcessed/didymos/redmine-YYYY"

# Uncomment and edit these as needed if generating INFO files from SPICE
# kernels. Only used in this case.
spiceKernelTop="$pipelineProcessed/dart/redmine-XXXX/liciacube/spice"

#-------------------------------------------------------------------------------


skipSection="true"
#-------------------------------------------------------------------------------
# This block is for updating or reusing a previously delivered or processed
# model.
#-------------------------------------------------------------------------------
createHardLinks $pipelineRawData/didymos/redmine-XXXX/didymos/ideal-impact4-20200629-v01 $rawDataTop/$outputTop
#-------------------------------------------------------------------------------


#-------------------------------------------------------------------------------
# Delivery to Raw-data.
#-------------------------------------------------------------------------------
srcTop="$deliveryTop"
destTop="$rawDataTop"

# Copying all delivered files to raw data should suffice for self-contained
# deliveries. For other cases, change variables above and/or do not copy all
# of "dot". copyDir has optional second argument to (re)name destination.
copyDir .
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# Raw-data to Processed. Depending on what is being delivered, include,
# comment out, and/or edit the following blocks.
#-------------------------------------------------------------------------------

# To update another processed area, uncomment to link to it here.
# linkToProcessedArea $areaToUpdate $processedTop
#-------------------------------------------------------------------------------

# Begin standard model block. This is for most model-associated items, and
# does not include image/spectra/lidar/spice related items. This block
# properly does not include a top-level copyDir command. Rather, each type
# of model-associated item has dedicated functions as shown below.
#-------------------------------------------------------------------------------
srcTop="$rawDataTop/$outputTop"
destTop="$processedTop/$outputTop"

# Generate complete set of model metadata.
generateModelMetadata $processedTop

# Process any/all standard model files.
processStandardModelFiles

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
copyDir .

# Unpack any archives that are present in the delivered files.
unpackArchives $destTop

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
#
# Directory in which to unpack SPICE files. Should be as short as possible
# due to SPICE path restrictions.
tmpSpiceDir="/project/sbmt2/$processingId"
bodyId="${bodyId^^}" # usually this is same as used for coloring, but all caps.
bodyFrame="920065803_FIXED" # Didymos-specific.
#bodyFrame="120065803_FIXED" # Dimorphos-specific.


# End common (all-instrument) header/setup
#-------------------------------------------------------------------------------

# Need an Instrument sub-block like below for each instrument in this delivery.


skipSection="true" # THIS SHOULD ALWAYS BE true WHEN CHECKING THIS IN!!!
# Instrument sub-block (ISS).
#-------------------------------------------------------------------------------
scId="cassini"
instrument="iss"

# Copy all delivered instrument files.
# copyDir $instrument
copyDir .

# Set up galleries (if present).
createGalleryList $destTop

#-------------------------------------------------------------------------------
# Process SUM files
checkSumFiles $destTop/$instrument
processMakeSumFiles $destTop/$instrument $scId/$instrument/images

# Update database tables.
# This symbolic link is needed because the database generator appends "/data"
# to the root URL, but this level of directory is not used in deliveries.
createRelativeLink $processedTop $processedTop/data

# This symbolic link is needed because the images are separate from the rest
# of this model's files. Spacecraft ID part of the path must be all lowercase.
createLink "$deployedTop/$scId" "$processedTop/$scId"

# Second argument is the pointing type. Supported values are the enumerations
# in the ImageSource class.
generateDatabaseTable ${instrument^^} GASKELL

# End Instrument sub-block (ISS).
#-------------------------------------------------------------------------------


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
  $bodyId $bodyFrame ${scId^^} $instrument $instFrame $timeKeyword \
  $processedTop $imageDir $infoFileDir
#-------------------------------------------------------------------------------

# Update database tables.
# This symbolic link is needed because the database generator appends "/data"
# to the root URL, but this level of directory is not used in deliveries.
createRelativeLink $processedTop $processedTop/data

generateDatabaseTable ${instrument^^} SPICE

# Set up galleries (if present).
createGalleryList "$imageTopDir/dart/$instrument/$modelId"

# End Instrument sub-block (DRACO).
#-------------------------------------------------------------------------------


skipSection="true" # THIS SHOULD ALWAYS BE true WHEN CHECKING THIS IN!!!

# Instrument sub-block (LEIA).
#-------------------------------------------------------------------------------
scId="LICIA"
instrument="leia"

# Copy all delivered instrument files.
# copyDir $instrument

#-------------------------------------------------------------------------------
# Process SPICE inputs for this sub-block. This sub-section is only needed
# if this delivery requires generating INFO files from SPICE kernels
# for this instrument. Otherwise, recommend uncommenting the following line:
# skipSection="true" THIS SHOULD ALWAYS BE true WHEN CHECKING THIS IN!!!

metakernel="kernels.tm" # relative to $tmpSpiceDir.
instFrame="LICIA_PL1" # THIS IS SPECIFIC TO LEIA.
# Get the images from the image delivery associated with this model.
imageTopDir="$pipelineRawData/dart/redmine-XXXX"
# Peculiar to DART SIMULATED models: images are in a model-specific directory
# under the mission/instrument directory.
imageDir="dart/$instrument/$modelId/images"
infoFileDir="$processedTop/$outputTop/$instrument/infofiles"
timeKeyword="IMG_UTC" # This is only used if extracting times from FITS files.

# Make SPICE kernels available in the temporary SPICE directory. This is so
# that any absolute paths in the metakernel may be edited to be as short
# as possible. Note there is only one temporary directory, so cannot
# simulateously process two deliveries that use SPICE kernels.
createLink $spiceKernelTop $tmpSpiceDir

# Generate the info files for the images from the SPICE kernels using times in
# FITS images. If the images don't have time stamps, can use
# createInfoFilesFromTimeStamps instead.
createInfoFilesFromFITSImages $metakernel \
  $bodyId $bodyFrame ${scId^^} $instrument $instFrame $timeKeyword \
  $imageTopDir $imageDir $infoFileDir

#-------------------------------------------------------------------------------

# Update database tables.
# This symbolic link is needed because the database generator appends "/data"
# to the root URL, but this level of directory is not used in deliveries.
createRelativeLink $processedTop $processedTop/data

generateDatabaseTable ${instrument^^} SPICE

# Set up galleries (if present).
createGalleryList "$imageTopDir/dart/$instrument/$modelId"

# End Instrument sub-block (LEIA).
#-------------------------------------------------------------------------------


skipSection="true" # THIS SHOULD ALWAYS BE true WHEN CHECKING THIS IN!!!

# Instrument sub-block (LUKE).
#-------------------------------------------------------------------------------
scId="LICIA"
instrument="luke"

# Copy all delivered instrument files.
# copyDir $instrument

#-------------------------------------------------------------------------------
# Process SPICE inputs for this sub-block. This sub-section is only needed
# if this delivery requires generating INFO files from SPICE kernels
# for this instrument. Otherwise, recommend uncommenting the following line:
# skipSection="true" THIS SHOULD ALWAYS BE true WHEN CHECKING THIS IN!!!

metakernel="kernels.tm" # relative to $tmpSpiceDir.
instFrame="LICIA_PL2" # THIS IS SPECIFIC TO LUKE.
# Get the images from the image delivery associated with this model.
imageTopDir="$pipelineRawData/dart/redmine-XXXX"
# Peculiar to DART SIMULATED models: images are in a model-specific directory
# under the mission/instrument directory.
imageDir="dart/$instrument/$modelId/images"
infoFileDir="$processedTop/$outputTop/$instrument/infofiles"
timeKeyword="IMG_UTC" # This is only used if extracting times from FITS files.

# Make SPICE kernels available in the temporary SPICE directory. This is so
# that any absolute paths in the metakernel may be edited to be as short
# as possible. Note there is only one temporary directory, so cannot
# simulateously process two deliveries that use SPICE kernels.
createLink $spiceKernelTop $tmpSpiceDir

# Generate the info files for the images from the SPICE kernels using times in
# FITS images. If the images don't have time stamps, can use
# createInfoFilesFromTimeStamps instead.
createInfoFilesFromFITSImages $metakernel \
  $bodyId $bodyFrame ${scId^^} $instrument $instFrame $timeKeyword \
  $imageTopDir $imageDir $infoFileDir

#-------------------------------------------------------------------------------

# Update database tables.
# This symbolic link is needed because the database generator appends "/data"
# to the root URL, but this level of directory is not used in deliveries.
createRelativeLink $processedTop $processedTop/data

generateDatabaseTable ${instrument^^} SPICE

# Set up galleries (if present).
createGalleryList "$imageTopDir/dart/$instrument/$modelId"

# End Instrument sub-block (LUKE).
#-------------------------------------------------------------------------------


# End Instrument processing sub-blocks.
#-------------------------------------------------------------------------------
