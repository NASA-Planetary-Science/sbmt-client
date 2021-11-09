#-------------------------------------------------------------------------------
# 05-processImages.sh
#
# This script segment is a template for processing SPICE kernels to
# produce INFO files.
#
# See 00-init.sh for more info about how the numbered script segments work.
#-------------------------------------------------------------------------------

scId="licia"
timeKeyword="IMG_UTC" # This is only used if extracting times from FITS files.

instrument="leia"
metakernel="leia.tm" # relative to $tmpSpiceDir.
instFrame="LICIA_PL-1" # THIS IS SPECIFIC TO LEIA.

# Get the images from the image delivery associated with this model.
imageTopDir="$processedTop"

tmpSpiceDir="/project/sbmtpipeline/spice"

spiceKernelTop="$processedTop/spice/$scId-corrected/ideal-impacts-20200629-v01"

# Unpack SPICE kernels in this area for the ideal impacts.
unpackArchives $spiceKernelTop

# Correct the FOV in the LEIA IK.
./correct-leia-ik-fov.sh "$spiceKernelTop/project/dart/data/SPICE/planning/ik/LICIA_PL1.TI"
check $? "Unable to correct FOV in LEIA IK."

# Make SPICE kernels available in the temporary SPICE directory. This is so
# that any absolute paths in the metakernel may be edited to be as short
# as possible. Note there is only one temporary directory, so cannot
# simulateously process two deliveries that use SPICE kernels.
createLink $spiceKernelTop $tmpSpiceDir

infofiles="infofiles-corrected"

for modelId in \
  "ideal-impact1-20200629-v01" \
  "ideal-impact2-20200629-v01" \
  "ideal-impact3-20200629-v01" \
  "ideal-impact4-20200629-v01" \
  "ideal-impact5-20200629-v01" \
  ; do

  # Peculiar to DART SIMULATED models: images are in a model-specific directory
  # under the mission/instrument directory.
  imageDir="dart/$instrument/$modelId/images"

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

spiceKernelTop="$processedTop/spice/$scId-corrected/errors-impacts-20200629-v01"

# Unpack SPICE kernels in this area for the ideal impacts.
unpackArchives $spiceKernelTop

./correct-leia-ik-fov.sh "$spiceKernelTop/project/dart/data/SPICE/planning/ik/LICIA_PL1.TI"
check $? "Unable to correct FOV in LEIA IK."

# Make SPICE kernels available in the temporary SPICE directory. This is so
# that any absolute paths in the metakernel may be edited to be as short
# as possible. Note there is only one temporary directory, so cannot
# simulateously process two deliveries that use SPICE kernels.
createLink $spiceKernelTop $tmpSpiceDir

for modelId in \
  "errors-impact1-20200629-v01" \
  "errors-impact2-20200629-v01" \
  "errors-impact3-20200629-v01" \
  "errors-impact4-20200629-v01" \
  "errors-impact5-20200629-v01" \
  ; do

  # Peculiar to DART SIMULATED models: images are in a model-specific directory
  # under the mission/instrument directory.
  imageDir="dart/$instrument/$modelId/images"

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
