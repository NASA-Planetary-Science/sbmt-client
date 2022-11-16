#-------------------------------------------------------------------------------
# 04-processImages.sh
#
# This script segment is a template for processsing images.
#
# See 00-init.sh for more info about how the numbered script segments work.
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# Set environment variables.
#-------------------------------------------------------------------------------
# Top level under which all images are located.
imageTopDir="$processedTop"
#-------------------------------------------------------------------------------

for modelId in \
  "ideal-impact1-20200629-v01" \
  "ideal-impact2-20200629-v01" \
  "ideal-impact3-20200629-v01" \
  "ideal-impact4-20200629-v01" \
  "ideal-impact5-20200629-v01" \
  "errors-impact1-20200629-v01" \
  "errors-impact2-20200629-v01" \
  "errors-impact3-20200629-v01" \
  "errors-impact4-20200629-v01" \ 
  "errors-impact5-20200629-v01" \
  ; do
  
  # Set up galleries for all instruments.
  createGalleryList "$imageTopDir/dart/draco/$modelId"
  createGalleryList "$imageTopDir/dart/leia/$modelId"
  createGalleryList "$imageTopDir/dart/luke/$modelId"
done
#-------------------------------------------------------------------------------
