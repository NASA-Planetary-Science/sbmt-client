#-------------------------------------------------------------------------------
# 05-processImages.sh
#
# This script segment is a template for processing SPICE kernels to
# produce INFO files.
#
# See 00-init.sh for more info about how the numbered script segments work.
#-------------------------------------------------------------------------------

scId="dart"
timeKeyword="COR_UTC" # This is only used if extracting times from FITS files.

instrument="draco"
metakernel="impact.tm" # relative to $tmpSpiceDir.
instFrame="DART_DRACO_2X2" # THIS IS SPECIFIC TO DRACO.

# Get the images from the image delivery associated with this model.
imageTopDir="$processedTop"

tmpSpiceDir="/project/sbmtpipeline/spice"

spiceKernelTop="$processedTop/spice/$scId/ideal-impacts-20200629-v01"

# Unpack SPICE kernels in this area for the ideal impacts.
unpackArchives $spiceKernelTop

# Make SPICE kernels available in the temporary SPICE directory. This is so
# that any absolute paths in the metakernel may be edited to be as short
# as possible. Note there is only one temporary directory, so cannot
# simulateously process two deliveries that use SPICE kernels.
createLink $spiceKernelTop $tmpSpiceDir

infofiles="infofiles"

for modelId in \
  "ideal-impact4-ra-20210211-v01" \
  ; do

  # Peculiar to DART SIMULATED models: images are in a model-specific directory
  # under the mission/instrument directory.
  # PECULIAR TO THIS delivery: use DRACO images from ideal impact 4.
  imageDir="dart/$instrument/ideal-impact4-20200629-v01/images"

  bodyId="didymos"
  bodyFrame="920065803_FIXED" # Didymos-specific.

  infoFileDir="$processedTop/$bodyId/$modelId/$instrument/$infofiles"
  
  # Generate the info files for the images from the SPICE kernels using times in
  # FITS images. If the images don't have time stamps, can use
  # createInfoFilesFromTimeStamps instead.
  createInfoFilesFromFITSImages $metakernel \
    ${bodyId^^} $bodyFrame ${scId^^} $instrument $instFrame $timeKeyword \
    $imageTopDir $imageDir $infoFileDir

  bodyId="dimorphos"
  bodyFrame="120065803_FIXED" # Dimorphos-specific.

  infoFileDir="$processedTop/$bodyId/$modelId/$instrument/$infofiles"
  
  # Generate the info files for the images from the SPICE kernels using times in
  # FITS images. If the images don't have time stamps, can use
  # createInfoFilesFromTimeStamps instead.
  createInfoFilesFromFITSImages $metakernel \
    ${bodyId^^} $bodyFrame ${scId^^} $instrument $instFrame $timeKeyword \
    $imageTopDir $imageDir $infoFileDir

done
#-------------------------------------------------------------------------------
